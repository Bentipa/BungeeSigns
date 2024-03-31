package me.Bentipa.BungeeSignsFree.sheduler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import me.Bentipa.BungeeSignsFree.BungeeSign;
import me.Bentipa.BungeeSignsFree.Core;
import me.Bentipa.BungeeSignsFree.ServerInfo;
import me.Bentipa.BungeeSignsFree.pinghelp.ServerPing;
import me.Bentipa.BungeeSignsFree.events.BSSBackSendEvent;
import me.Bentipa.BungeeSignsFree.events.BSSPingEvent;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Bentipa(Benjamin)
 * @year 2016
 */
public class BungeeSignsPinger implements Runnable, Listener {

    private final me.Bentipa.BungeeSignsFree.Core plugin;

    private final BungeeSignsPinger instance;

    public BungeeSignsPinger(me.Bentipa.BungeeSignsFree.Core bSignsMain) {
        this.instance = this;
        this.plugin = bSignsMain;
        bSignsMain.getServer().getPluginManager().registerEvents(this, bSignsMain);
    }

    public void start() {
        Core.getInstance().getLogger().log(Level.INFO, "Refresh time: " + (plugin.getConfig().getInt("sign-refresh") / 1000) * 20);
//        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, (plugin.getConfig().getInt("sign-refresh") / 1000) * 20, 0);
        BukkitRunnable runnable = new BukkitRunnable() {

            @Override
            public void run() {
                instance.run();
            }
        };
        runnable.runTaskTimerAsynchronously(plugin, 0, (plugin.getConfig().getInt("sign-refresh") / 1000) * 20L);
    }

    @Override
    public void run() {
        final List<ServerInfo> servers = plugin.servers;
        final BSSPingEvent event = new BSSPingEvent(servers);
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getPluginManager().callEvent(event);
            }
        };
        bukkitRunnable.runTaskLater(plugin, 1);
        if (Core.DEBUG) {
            Core.getInstance().getLogger().log(Level.INFO, "Called BSSPingEvents for " + servers.size() + " Servers!");
        }
    }

    @EventHandler
    public void onEvent(BSSPingEvent e) {
        if (!e.isCancelled()) {
            if (Core.DEBUG) {
                Core.getInstance().getLogger().log(Level.INFO, "Received BSSPingEvents for " + e.getServers().size() + " Servers!");
            }
            for (final ServerInfo server : e.getServers()) {
                if (server.isLocal()) {
                    if (Core.DEBUG) {
                        Core.getInstance().getLogger().log(Level.INFO, "local");
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                        @Override
                        public void run() {
                            server.setMotd(Bukkit.getMotd());
                            server.setPlayerCount(Bukkit.getOnlinePlayers().size());
                            server.setMaxPlayers(Bukkit.getMaxPlayers());
                            server.setPingStart(System.currentTimeMillis());
                            server.setPingEnd(System.currentTimeMillis());

                            BSSBackSendEvent backsend = new BSSBackSendEvent(server, null);
                            plugin.callSyncEvent(backsend);
                        }
                    });
                } else {
                    if (Core.DEBUG) {
                        Core.getInstance().getLogger().log(Level.INFO, "Ext.!");
                    }
                    pingAsync(server);
                }

            }
        }
    }

    @EventHandler
    public void onEvent(BSSBackSendEvent e) {
        if (!e.isCancelled()) {
            ServerInfo si = e.getServerInfo();
            for (BungeeSign bs : getSigns(si.getName())) {
                bs.setServerInfo(si);
            }
        }
    }

    private ArrayList<BungeeSign> getSigns(String servername) {
        ArrayList<BungeeSign> signs = new ArrayList<BungeeSign>();
        for (BungeeSign bs : plugin.getSigns()) {
            if (bs.getServer().equals(servername)) {
                signs.add(bs);
            }
        }
        return signs;
    }

    private void pingAsync(final ServerInfo server) {
        final ServerPing ping = server.getPing();
        if (Core.DEBUG) {
            Core.getInstance().getLogger().log(Level.INFO, "Pinging Async!");
        }
        if (!ping.isFetching()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    long pingStartTime = System.currentTimeMillis();
                    ping.setAddress(server.getAddress());
                    ping.setTimeout(server.getTimeout());
                    ping.setFetching(true);

                    if (Core.DEBUG) {
                        Core.getInstance().getLogger().log(Level.INFO, "Starting!");
                    }
                    try {
                        ServerPing.SResponse response = ping.fetchData();
                        server.setMotd(response.description);
                        server.setPlayerCount(response.players);
                        server.setMaxPlayers(response.slots);
                        server.setOnline(true);
                        server.setPingStart(pingStartTime);
                        server.setVersion(response.version);
                        server.setFailedConnections(0);
                        if (Core.DEBUG) {
                            Core.getInstance().getLogger().log(Level.INFO, "Fetched Data! {" + server.getName() + "}");
                            Core.getInstance().getLogger().log(Level.INFO, "Motd: " + response.description);
                            Core.getInstance().getLogger().log(Level.INFO, "Players: " + response.players);
                            Core.getInstance().getLogger().log(Level.INFO, "Slots: " + response.slots);
                        }
                    } catch (Exception e) {
                        server.setOnline(false);
                        server.setPlayerCount(0);
                        server.setFailedConnections(server.getFailedConnections() + 1);
                        if (Core.DEBUG) {
                            Core.getInstance().getLogger().log(Level.INFO, "Ping failed!");
                            e.printStackTrace();
                        }
                    } finally {
                        if (Core.DEBUG) {
                            Core.getInstance().getLogger().log(Level.INFO, "Finished!");
                        }
                        ping.setFetching(false);
                        server.setPingEnd(System.currentTimeMillis());
                    }

                    BSSBackSendEvent backsend = new BSSBackSendEvent(server, ping);
                    plugin.callSyncEvent(backsend);
                }
            });
        }
    }

}
