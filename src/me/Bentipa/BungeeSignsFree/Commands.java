/*
 * stealth-coders (c) 2016 
 * Copyright by stealth-coders:
 * You are NOT allowed to share, upload or decompile this plugin at any time.
 * You are NOT allowed to share, upload or use code parts/snippets of this plugin without our consent.
 * You are allowed to use this software only for yourself and/or your server/servers.
 * The respective Owner of this Software is stealth-coders.
 */
package me.Bentipa.BungeeSignsFree;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import static me.Bentipa.BungeeSignsFree.Core.inCreation;
import de.stealthcoders.Bentipa.bungeecloud.saving.BungeeCordConfigGetter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

/**
 *
 * @author Benjamin
 */
public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
            String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("bsinfo")) {
                if (player.hasPermission("BungeeSigns.info")) {
                    player.sendMessage(Core.getInstance().MSG_PREFIX() + ChatColor.GREEN
                            + "[Info] Created at 03.04.2016 by Bentipa");

                    return true;
                } else {
                    player.sendMessage(ChatColor.RED
                            + "You don't have the permission to use this command!");
                    return false;
                }
            }
            if (cmd.getName().equalsIgnoreCase("createBSSign")) {
                if (player.hasPermission("BungeeSigns.create")) {
                    player.sendMessage(Core.getInstance().SS(Core.Step.SELECT)
                            + ChatColor.GREEN
                            + "Click on the Sign you want to be a Bungee-Sign.");
                    inCreation.add(player);
                    Core.creationStep.put(player, Core.Step.SELECT);
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED
                            + "You don't have the permission to use this command!");
                    return false;
                }
            }
            if (cmd.getName().equalsIgnoreCase("removeBSSign")) {
                if (player.hasPermission("BungeeSigns.remove")) {
                    player.sendMessage(ChatColor.GREEN
                            + "Click on the Sign you dont want to be a Bungee-Sign.");
                    Core.inRemove.add(player);
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED
                            + "You don't have the permission to use this command!");
                    return false;
                }
            }
            if (cmd.getName().equalsIgnoreCase("bsreload")) {
                if (player.hasPermission("BungeeSigns.reload")) {
                    try {
                        Core.getInstance().getConfig().load(new File("plugins/BungeeServerSigns/config.yml"));
                        BungeeCordConfigGetter bccg = new BungeeCordConfigGetter(Core.getInstance());
                        for (String key : bccg.getConfig().getServers().keySet()) {
                            InetSocketAddress adr = bccg.getConfig().getServers().get(key);
                            Core.getInstance().servers.add(new ServerInfo(key, key, adr.getHostString(), adr
                                    .getPort(), 40));
                        }
                    } catch (IOException | InvalidConfigurationException e1) {
                        // TODO Auto-generated catch block
                        Core.getInstance().getLogger().severe("Couldnt find config.yml!");
                    }
                    player.sendMessage(ChatColor.GREEN
                            + "Reloaded the config.");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED
                            + "You don't have the permission to use this command!");
                    return false;
                }
            }

        }
        sender.sendMessage("You can only issues this command as a player!");
        return false;
    }

}
