/*
 * stealth-coders (c) 2016  All rights reserved.
 * Copyright by stealth-coders:
 * You are NOT allowed to share, upload or decompile this plugin at any time.
 * You are NOT allowed to share, upload or use code parts/snippets of this plugin without our consent.
 * You are allowed to use this software only for yourself and/or your server/servers.
 * The respective Owner of this Software is stealth-coders.
 */
package me.Bentipa.BungeeSignsFree;

import java.util.ArrayList;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Benjamin
 */
public class BungeeSignLoader {

    public static ArrayList<BungeeSign> loadSigns(YamlConfiguration config) {
        ArrayList<BungeeSign> ret = new ArrayList<>();
        
        if(config.getConfigurationSection("signs")==null){return ret;}
        for (String server : config.getConfigurationSection("signs").getKeys(false)) {
            String name = server;
            server="signs."+server;
            VirtualLocation vl = new VirtualLocation(config.getDouble(server+".location.x"), config.getDouble(server+".location.y"), config.getDouble(server+".location.z"), config.getString(server+".location.world"));
            BungeeSign bs = new BungeeSign(Core.getInstance(), vl, Core.getInstance().retrieveServerInfo(name));
            bs.setLine(0, config.getString(server+".lines.1"));
            bs.setLine(1, config.getString(server+".lines.2"));
            bs.setLine(2, config.getString(server+".lines.3"));
            bs.setLine(3, config.getString(server+".lines.4"));
            ret.add(bs);
            Core.getInstance().getLogger().log(Level.INFO, "Loaded Sign {0}!", server);
        }
        return ret;
    }
    
    public static void saveSigns(ArrayList<BungeeSign> signs, YamlConfiguration config){
        for(BungeeSign sign : signs){
            config.set("signs."+sign.getServer()+".location.x", sign.getVLocation().getX());
            config.set("signs."+sign.getServer()+".location.y", sign.getVLocation().getY());
            config.set("signs."+sign.getServer()+".location.z", sign.getVLocation().getZ());
            config.set("signs."+sign.getServer()+".location.world", sign.getVLocation().getWorldname());
            config.set("signs."+sign.getServer()+".lines.1", sign.getLine(0));
            config.set("signs."+sign.getServer()+".lines.2", sign.getLine(1));
            config.set("signs."+sign.getServer()+".lines.3", sign.getLine(2));
            config.set("signs."+sign.getServer()+".lines.4", sign.getLine(3));  
            
        }        
    }

}

