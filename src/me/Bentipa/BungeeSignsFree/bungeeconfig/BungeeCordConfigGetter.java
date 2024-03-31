/*
 * stealth-coders (c) 2016
 * Copyright by stealth-coders:
 * You are NOT allowed to share, upload or decompile this plugin at any time.
 * You are NOT allowed to share, upload or use code parts/snippets of this plugin without our consent.
 * You are allowed to use this software only for yourself and/or your server/servers.
 * The respective Owner of this Software is stealth-coders.
 */
package de.stealthcoders.Bentipa.bungeecloud.saving;


import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import me.Bentipa.BungeeSignsFree.Core;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author Bentipa(Benjamin)
 * @year 2016
 *
 */
public class BungeeCordConfigGetter {

    private BungeeConfig bc;

    public BungeeCordConfigGetter(Core c) {

        File f = new File(c.getDataFolder() + "/bungeeconfig.yml");
        boolean fileCopied = true;
        if (f.exists()) {
            c.sendLogMessage("BungeeConfig File found!");
            fileCopied = true;
        } else {
            try {
                f.createNewFile();
                c.getLogger().info("Created BungeeConfig File - Copy now yours and replace the created one.");
                fileCopied = false;
            } catch (IOException e) {
                c.getLogger().info("Could not create BungeeConfig File. [No Permissions?]");
                fileCopied = false;
            }

        }
        if (fileCopied) {
            YamlConfiguration bconfig = YamlConfiguration.loadConfiguration(f);
            if (bconfig.contains("servers")) {
                ConfigurationSection servers = bconfig.getConfigurationSection("servers");
                HashMap<String, InetSocketAddress> servershash = new HashMap<>();

                Set<String> servernames = servers.getKeys(false);
                c.getLogger().info("################################");
                for (String serv : servernames) {
                    c.getLogger().log(Level.INFO, "Found Server: {0}", serv);
                    String val = servers.getString(serv + ".address");
                    String[] vals = val.split(":");
                    servershash.put(serv, new InetSocketAddress(vals[0], Integer.valueOf(vals[1])));
                    c.getLogger().log(Level.INFO, "With Address:  {0}", val);
                    c.getLogger().info("################################");
                }
                bc = new BungeeConfig(servershash);
            }
        }
    }

    public BungeeConfig getConfig() {
        return bc;
    }

    public class BungeeConfig {

        private HashMap<String, InetSocketAddress> servers;

        public BungeeConfig(HashMap<String, InetSocketAddress> servers) {
            this.setServers(servers);
        }

        public HashMap<String, InetSocketAddress> getServers() {
            return servers;
        }

        private void setServers(HashMap<String, InetSocketAddress> servers) {
            this.servers = servers;
        }
    }
}
