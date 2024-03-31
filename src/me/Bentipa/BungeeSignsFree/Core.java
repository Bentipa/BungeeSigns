package me.Bentipa.BungeeSignsFree;

import de.stealthcoders.Bentipa.bungeecloud.saving.BungeeCordConfigGetter;
import me.Bentipa.BungeeSignsFree.metrics.Metrics;
import me.Bentipa.BungeeSignsFree.sheduler.BungeeSignsPinger;
import me.Bentipa.BungeeSignsFree.sheduler.BungeeSignsRefresher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class Core extends JavaPlugin {

    public static boolean checkEvents;

    protected static ArrayList<Player> inCreation = new ArrayList<>();
    protected static ArrayList<Player> inRemove = new ArrayList<>();
    protected static HashMap<Player, Step> creationStep = new HashMap<>();
    protected static HashMap<Player, BungeeSign> creations = new HashMap<>();

    private ArrayList<BungeeSign> signs = new ArrayList<>();
    private BungeeSignsRefresher bsr;

    private String servername;

    public ArrayList<ServerInfo> servers = new ArrayList<>();

    private BungeeSignsPinger pinger;

    public String ENTER_MSG = "";
    public boolean SERVER_ALIVE = true;
    public int TIMEOUT = 0;

    private static Core inst;
    public static boolean DEBUG = false;

    @Override
    public void onEnable() {
        inst = this;
        loadConfig();
        SERVER_ALIVE = getConfig().getBoolean("server-alive");
        this.getLogger().info("[Info] Config loaded ");
        getServer().getPluginManager().registerEvents(new BSignsListener(this), this);

        BungeeCordConfigGetter bccg = new BungeeCordConfigGetter(this);
        if (bccg.getConfig() == null) {
            sendLogMessage(ChatColor.RED + "You have to copy the content of your BungeeCord config.yml into bungeeconfig.yml !");
            getPluginLoader().disablePlugin(this);
            return;
        }
        if (bccg.getConfig().getServers() == null || bccg.getConfig().getServers().isEmpty()) {
            sendLogMessage(ChatColor.RED + "You have to copy the content of your BungeeCord config.yml into bungeeconfig.yml !");
        } else {
            for (String key : bccg.getConfig().getServers().keySet()) {
                InetSocketAddress adr = bccg.getConfig().getServers().get(key);
                servers.add(new ServerInfo(key, key, adr.getHostString(), adr
                        .getPort(), 40));
            }
        }

        TIMEOUT = getConfig().getInt("timeout");
        ENTER_MSG = getConfig().getString("enter-msg");

        loadSigns();
        this.getLogger().info("[Info] Signs loaded!");

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        try {
            bsr = new BungeeSignsRefresher(this);
            bsr.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.getLogger().info("[Info] Refresh-Task started!");

        this.getLogger().info("[Info] Plugin enabled ");

        this.pinger = new BungeeSignsPinger(this);

        Commands cmds = new Commands();
        getCommand("createbssign").setExecutor(cmds);
        getCommand("removebssign").setExecutor(cmds);
        getCommand("bsinfo").setExecutor(cmds);
        getCommand("bsreload").setExecutor(cmds);
        DEBUG = getConfig().getBoolean("debug");
        if (DEBUG) {
            sendLogMessage(ChatColor.RED + "### DEBUG MODE ON ###");
        }

        Metrics metrics = new Metrics(this);

        long time = (long) (10.3 * 20L);
        final Core instance = this;
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                //START SCHEDULERS
                Bukkit.getScheduler().runTaskLater(instance, bsr, 40L);
                pinger.start();
                getLogger().info("[Info] Started Pinger and Refresher!");
            }
        }, time);

    }

    public static Core getInstance() {
        return inst;
    }

    public void callSyncEvent(final Event event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                getServer().getPluginManager().callEvent(event);
            }
        });
    }

    public ArrayList<ServerInfo> getServerInfos() {
        return this.servers;
    }

    public String getServerName() {
        return this.servername;
    }

    public void setServerName(String s) {
        this.servername = s;
    }

    @Override
    public void onDisable() {
        this.getLogger().info("[Info] Refresh-Task stopped!");
        this.getLogger().info("[Info] Plugin disabled!");
    }

    protected String MSG_PREFIX() {
        return ChatColor.GRAY + "[" + ChatColor.BLUE + "BungeeSigns"
                + ChatColor.GRAY + "] ";
    }

    protected enum Step {

        SELECT, SERVER_NAME, SIGN_CONTENT, END;
    }

    protected String SS(Step s) {
        switch (s) {
            case SELECT:
                return ChatColor.GRAY + "[" + ChatColor.AQUA + "Step: "
                        + ChatColor.RED + "1" + ChatColor.GRAY + "] ";
            case SERVER_NAME:
                return ChatColor.GRAY + "[" + ChatColor.AQUA + "Step: "
                        + ChatColor.YELLOW + "2" + ChatColor.GRAY + "] ";
            case SIGN_CONTENT:
                return ChatColor.GRAY + "[" + ChatColor.AQUA + "Step: "
                        + ChatColor.BLUE + "3" + ChatColor.GRAY + "] ";

        }
        return "";
    }

    public boolean isSaved(Location loc) {
        for (BungeeSign ffs : signs) {
            if (ffs != null) {
                if (ffs.getLocation().equals(loc)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void sendLogMessage(String msg) {
        getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + getDescription().getName() + ChatColor.GRAY + "] " + ChatColor.WHITE + msg);
    }

    public Sign getSign(BungeeSign ffs) {
        for (BungeeSign s : signs) {
            if (ffs.equals(s)) {
                if (s.getLocation().getWorld().getBlockAt(s.getLocation())
                        .getState() instanceof Sign) {
                    return (Sign) s.getLocation().getWorld()
                            .getBlockAt(s.getLocation()).getState();
                }
            }
        }
        for (BungeeSign s : creations.values()) {
            if (ffs.equals(s)) {
                if (s.getLocation().getWorld().getBlockAt(s.getLocation())
                        .getState() instanceof Sign) {
                    return (Sign) s.getLocation().getWorld()
                            .getBlockAt(s.getLocation()).getState();
                }
            }
        }
        return null;
    }

    private boolean isSignBlock(Block block) {
        if (DEBUG) {
            this.getLogger().log(Level.INFO, "Checking if block is sign: sign in " + block.getType().name().toLowerCase() + "?");
        }
        return block.getType().name().toLowerCase().contains("sign");
    }

    public BungeeSign getBungeeSignsSign(Location loc) {
        if (!isSaved(loc)) {
            return null;
        }

        for (BungeeSign ffs : signs) {
            if (ffs.getLocation().equals(loc)) {
                return ffs;
            }
        }

        return null;
    }

    public void saveSign(BungeeSign ffs) {
        signs.add(ffs);
        saveSigns();
    }

    public ArrayList<BungeeSign> getSigns() {
        return this.signs;
    }

    public void removeSign(BungeeSign ffs) {
        signs.remove(ffs);
        saveSigns();
    }

    private void saveSigns() {
        BungeeSignLoader.saveSigns(signs, getConfig());
        saveConfig();
    }

    private void loadSigns() {
        signs = BungeeSignLoader.loadSigns(getConfig());
    }

    private YamlConfiguration config;

    public void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getDataFolder().mkdirs();
            saveResource("config.yml", true);
        }
        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
        }
        config.options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public YamlConfiguration getConfig() {
        return config;
    }

    public ServerInfo retrieveServerInfo(String name) {
        if (getServerInfo(name) != null) {
            return new ServerInfo(name, name, getServerInfo(name).getAddress().getHostName(), getServerInfo(name).getAddress().getPort(), TIMEOUT);
        }
        return null;
    }

    public ServerInfo getServerInfo(String servername) {
        for (ServerInfo si : servers) {
            if (si.getName().equals(servername)) {
                return si;
            }
        }
        return null;
    }

}
