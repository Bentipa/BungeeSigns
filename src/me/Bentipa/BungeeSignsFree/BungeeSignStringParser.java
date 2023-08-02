/*
 * stealth-coders (c) 2016 
 * Copyright by stealth-coders:
 * You are NOT allowed to share, upload or decompile this plugin at any time.
 * You are NOT allowed to share, upload or use code parts/snippets of this plugin without our consent.
 * You are allowed to use this software only for yourself and/or your server/servers.
 * The respective Owner of this Software is stealth-coders.
 */
package me.Bentipa.BungeeSignsFree;

import java.util.HashMap;

/**
 *
 * @author Benjamin
 */
public class BungeeSignStringParser {

    public static String getString(BungeeSign bs, int line, String ph) {
//        System.out.println(">> getString("+bs+", "+ line +", " + ph +")");
        
        HashMap<String, String> placeholders = new HashMap<>();
        
        if (bs.getServerInfo() == null) {
            return ph;
        }

        if (ph.contains("%motd%")) {
            ph = ph.replace("%motd%", bs.getServerInfo().getMotd() == null ? "" : bs.getServerInfo().getMotd());
            placeholders.put("%motd%", bs.getServerInfo().getMotd());
        }

        if (ph.contains("%cplayers%")) {
            ph = ph.replace("%cplayers%", bs.getServerInfo().getPlayerCount() + "");
            placeholders.put("%cplayers%", bs.getServerInfo().getPlayerCount()+"");
        }        

        if (ph.contains("%mplayers%")) {
            ph = ph.replace("%mplayers%", bs.getServerInfo().getMaxPlayers() + "");
            placeholders.put("%mplayers%", bs.getServerInfo().getMaxPlayers() + "");
        }
            
        return ph;
    }
   
}