package me.Bentipa.BungeeSignsFree;

import java.util.HashMap;

import me.Bentipa.BungeeSignsFree.Core.Step;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Location;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Benjamin
 */
public class BSignsListener implements Listener {

    static me.Bentipa.BungeeSignsFree.Core core;

    public BSignsListener(me.Bentipa.BungeeSignsFree.Core bSignsMain) {
        core = bSignsMain;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block != null) {
            if (block.getState() instanceof Sign) {
                Sign s = (Sign) block.getState();
                Location loc = s.getLocation();
                if (Core.inCreation.contains(p) && Core.creationStep.containsKey(p)) {

                    if (Core.creationStep.get(p) == Core.Step.SELECT) {

                        Core.creations.put(p, new BungeeSign(core, new VirtualLocation(block.getLocation())));
                        p.sendMessage(core.SS(Step.SELECT) + ChatColor.GREEN + "Succesfully set BungeeSigns-Sign (" + ChatColor.GOLD + block.getX() + ChatColor.GREEN + "|" + ChatColor.GOLD + block.getY() + ChatColor.GREEN + "|" + ChatColor.GOLD + block.getZ() + ChatColor.GREEN + ")!");
                        Core.creationStep.put(p, Step.SERVER_NAME);
                        p.sendMessage(core.SS(Step.SERVER_NAME) + ChatColor.AQUA + "Now type in the name of to Server to connect to!");
                    }
                } else if (Core.inRemove.contains(p)) {
                    if (core.isSaved(loc)) {
                        core.removeSign(core.getBungeeSignsSign(loc));
                        p.sendMessage(ChatColor.GREEN + "Sign succesfully removed!");
                    } else {
                        p.sendMessage(ChatColor.RED + "This Sign is not a BungeeSign-Sign!");
                    }
                    Core.inRemove.remove(p);
                }
                if (p.hasPermission("BungeeSigns.use")) {
                    // Check interaction model
                    String mouse = Core.getInstance().getConfig().getString("teleport-mouse");
                    String sneak = Core.getInstance().getConfig().getString("teleport-sneak");
                    boolean pass = false;
                    if (mouse.equalsIgnoreCase("both")) {
                        pass = event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK;
                    } else if (mouse.equalsIgnoreCase("left")) {
                        pass = event.getAction() == Action.LEFT_CLICK_BLOCK;
                    } else if (mouse.equalsIgnoreCase("right")) {
                        pass = event.getAction() == Action.RIGHT_CLICK_BLOCK;
                    }
                    if (!(sneak.equalsIgnoreCase("ignore") || sneak.equalsIgnoreCase("false")) && !event.getPlayer().isSneaking()) {
                        pass = false;
                    }
                    if (sneak.equalsIgnoreCase("false") && event.getPlayer().isSneaking()) {
                        pass = false;
                    }
                    if (!pass) {
                        return;
                    }
                    if (core.isSaved(loc)) {
                        if (Core.DEBUG) {
                            System.out.println("Click: User clicked on sign, trying to connect!");
                        }
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();//
                        if (core.ENTER_MSG != null && core.getBungeeSignsSign(loc) != null && Core.getInstance().getConfig().getBoolean("send-msg")) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', core.ENTER_MSG.replace("%server", core.getBungeeSignsSign(loc).getServer())));
                        }

                        out.writeUTF("Connect");
                        out.writeUTF(core.getBungeeSignsSign(loc).getServer());
                        p.sendPluginMessage(core, "BungeeCord", out.toByteArray());
                    }
                } else {
                    if (Core.DEBUG) {
                        System.out.println("Click: User does not have the correct permission.");
                    }
                }
            } else {
                if (Core.DEBUG) {
                    System.out.println("Click: User clicked not on a sign.");
                }
            }
        } else {
            if (Core.DEBUG) {
                System.out.println("Click: Block is null");
            }
        }

    }

    private HashMap<Player, Integer> line = new HashMap<Player, Integer>();
    private HashMap<Player, Sign> ce = new HashMap<Player, Sign>();

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent e) {
        if (Core.inCreation.contains(e.getPlayer())) {
            if (Core.creationStep.get(e.getPlayer()).equals(Core.Step.SERVER_NAME)) {
                String message = e.getMessage();
                if (!message.contains("!")
                        && !message.contains(" ")
                        && !message.contains(".")
                        && !message.contains(",")
                        && !message.contains("_")
                        && !message.contains(";")) {
                    if (serverExists(message) || !core.SERVER_ALIVE) {
                        Core.creations.get(e.getPlayer()).setServerInfo(core.retrieveServerInfo(e.getMessage()));
                        e.getPlayer().sendMessage(core.SS(Step.SERVER_NAME) + ChatColor.GREEN + "Succesfully set Server to '" + ChatColor.GOLD + e.getMessage() + ChatColor.GREEN + "' !");

                        Core.creationStep.put(e.getPlayer(), Step.SIGN_CONTENT);
                        e.getPlayer().sendMessage(core.SS(Step.SIGN_CONTENT) + ChatColor.AQUA + "Now type in the lines of the Sign:");
                        line.put(e.getPlayer(), 1);
                        e.getPlayer().sendMessage(core.SS(Step.SIGN_CONTENT) + ChatColor.GREEN + "Line " + ChatColor.RED + line.get(e.getPlayer()) + ChatColor.GREEN + ":");
                        BukkitRunnable runnable = new BukkitRunnable() {
                            @Override
                            public void run() {
                                ce.put(e.getPlayer(), core.getSign(Core.creations.get(e.getPlayer())));
                            }
                        };
                        runnable.runTask(core);
                        e.setCancelled(true);
                    } else {
                        e.getPlayer().sendMessage(ChatColor.RED + "Server not found! [Try again and check spelling] !");
                        e.getPlayer().sendMessage(ChatColor.AQUA + "Availiable Servers:");
                        for (ServerInfo si : core.getServerInfos()) {
                            e.getPlayer().sendMessage(ChatColor.GOLD + si.getName());
                        }
                        e.setCancelled(true);
                    }
                } else {
                    e.getPlayer().sendMessage(ChatColor.RED + "Invalid characters!: (do not use '" + ChatColor.BLUE + "!" + ChatColor.RED + "', '" + ChatColor.BLUE + " " + ChatColor.RED + "', '"
                            + ChatColor.BLUE + "." + ChatColor.RED + "', '" + ChatColor.BLUE + "," + ChatColor.RESET + ChatColor.RED + "', '" + ChatColor.BLUE + ";" + ChatColor.RED + "', '" + ChatColor.BLUE + "_" + ChatColor.RED + "')");
                    e.setCancelled(true);
                }

            } else if (Core.creationStep.get(e.getPlayer()).equals(Core.Step.SIGN_CONTENT)) {
                if (line.get(e.getPlayer()) != 5) {
                    String msg = e.getMessage();

                    msg = msg.trim();

                    BungeeSign bs = Core.creations.get(e.getPlayer());
                    if (msg.contains("%cswitch(")) {
                        e.getPlayer().sendMessage(ChatColor.RED + "Not availiable in the Demo Version!");
                    }
                    if (msg.contains("%state(")) {
                        e.getPlayer().sendMessage(ChatColor.RED + "Not availiable in the Demo Version!");
                    }
                    if (msg.contains("%cplayers%")) {
                        e.getPlayer().sendMessage(core.SS(Step.SIGN_CONTENT) + ChatColor.LIGHT_PURPLE + "You added a " + ChatColor.GOLD + "Players-Display" + ChatColor.LIGHT_PURPLE + "!");
                    }
                    if (msg.contains("%mplayers%")) {
                        e.getPlayer().sendMessage(core.SS(Step.SIGN_CONTENT) + ChatColor.LIGHT_PURPLE + "You added a " + ChatColor.GOLD + "Players-Display" + ChatColor.LIGHT_PURPLE + "!");
                    }
                    if (msg.contains("%motd%")) {
                        e.getPlayer().sendMessage(core.SS(Step.SIGN_CONTENT) + ChatColor.LIGHT_PURPLE + "You added a " + ChatColor.GOLD + "Motd-Display" + ChatColor.LIGHT_PURPLE + "!");
                    }
                    if (msg.contains("%state%")) {
                        e.getPlayer().sendMessage(ChatColor.RED + "Not availiable in the Demo Version!");
                    }
                    if (msg.contains("%playersgra%")) {
                        e.getPlayer().sendMessage(ChatColor.RED + "Not availiable in the Demo Version!");
                    }

                    if (msg.equalsIgnoreCase("[SPACE]")) {
                        msg = "";
                    }
                    bs.setLine((line.get(e.getPlayer()) - 1), msg);
                    if (ce.get(e.getPlayer()) != null)
                        ce.get(e.getPlayer()).setLine((line.get(e.getPlayer()) - 1), msg);


                    e.getPlayer().sendMessage(core.SS(Step.SIGN_CONTENT) + ChatColor.GREEN + "Line " + ChatColor.RED + line.get(e.getPlayer()) + ChatColor.GREEN + " set to: '" + ChatColor.RESET + msg + ChatColor.GREEN + "'");
                    line.put(e.getPlayer(), line.get(e.getPlayer()) + 1);
                    if (line.get(e.getPlayer()) == 5) {
                        core.saveSign(Core.creations.get(e.getPlayer()));
                        BukkitRunnable runnable = new BukkitRunnable() {
                            @Override
                            public void run() {
                                Sign realsign = core.getSign(Core.creations.get(e.getPlayer()));
                                realsign.setLine(0, ce.get(e.getPlayer()).getLine(0));
                                realsign.setLine(1, ce.get(e.getPlayer()).getLine(1));
                                realsign.setLine(2, ce.get(e.getPlayer()).getLine(2));
                                realsign.setLine(3, ce.get(e.getPlayer()).getLine(3));
                                realsign.update(true);
                            }
                        };
                        runnable.runTask(core);
                        e.getPlayer().sendMessage(ChatColor.GREEN + "Succesfully created BungeeSigns-Sign!");
                        Core.inCreation.remove(e.getPlayer());
                    } else {
                        e.getPlayer().sendMessage(core.SS(Step.SIGN_CONTENT) + ChatColor.GREEN + "Line " + ChatColor.BLUE + line.get(e.getPlayer()) + ChatColor.GREEN + ":");
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    public boolean serverExists(String servername) {
        for (ServerInfo si : core.getServerInfos()) {
            if (si.getName().equals(servername)) {
                return true;
            }
        }
        return false;
    }

}
