package me.Bentipa.BungeeSignsFree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class BungeeSign {

    private VirtualLocation loc;
    private HashMap<Integer, String> lines = new HashMap<>();
    private Sign realsign;
    private me.Bentipa.BungeeSignsFree.Core core;
    private ServerInfo si;

    public BungeeSign(me.Bentipa.BungeeSignsFree.Core bSignsMain, VirtualLocation loc, ServerInfo server) {
        this.loc = loc;
        this.core = bSignsMain;
        this.si = server;
        initRealsign();
    }

    private boolean errorMessageSent = false;

    private void initRealsign() {
        try {
            realsign = (Sign) loc.toRealLocation().getWorld().getBlockAt(loc.toRealLocation()).getState();
        } catch (Exception e) {
            // Unable to set Sign!  
            if (!errorMessageSent) {
                Bukkit.getLogger().log(Level.SEVERE, "Error in loading/creating BungeeSign which connects to Server ''{0}'' with Address ''{1}''", new String[]{si.getName(), si.getAddress().toString()});
                e.printStackTrace();
                errorMessageSent = true;
            }
        }
    }

    public BungeeSign(me.Bentipa.BungeeSignsFree.Core bSignsMain, VirtualLocation loc) {
        this(bSignsMain, loc, null);
    }

    public void setSign(Sign s) {
        this.realsign = s;
    }

    public Sign getSign() {
        return this.realsign;
    }

    @Override
    public String toString() {
        String positions = getLocation().getBlockX() + "," + getLocation().getBlockY() + "," + getLocation().getBlockZ() + "," + getLocation().getWorld().getName();
        String lines = " " + getLine(0).replace(" ", "_") + " " + getLine(1).replace(" ", "_") + " " + getLine(2).replace(" ", "_") + " " + getLine(3).replace(" ", "_");
        String server = getServer();
        return server + " " + positions + " " + lines;
    }

    public boolean equals(Sign s) {
        Location sl = s.getLocation();
        if (sl.getBlockX() == getLocation().getBlockX()
                && sl.getBlockY() == getLocation().getBlockY()
                && sl.getBlockZ() == getLocation().getBlockZ()
                && sl.getWorld() == getLocation().getWorld()) {
            return true;
        }

        return false;
    }

    public void setLine(int line, String con) {
        if (line < 5 && line >= 0) {
            this.lines.put(line, con);
        }
    }

    public String getLine(int line) {
        if (line < 5 && line >= 0) {
            return this.lines.get(line);
        } else {
            return "";
        }
    }

    public ArrayList<String> getRawLines() {
        return new ArrayList<>(this.lines.values());
    }

    public Location getLocation() {
        return this.loc.toRealLocation();
    }

    public VirtualLocation getVLocation() {
        return this.loc;
    }

    public String getServer() {
        return this.si.getName();
    }

    public void refresh() {
        if (this.realsign == null) {
            initRealsign();
        }
        try {
            if (Core.DEBUG) {
                System.out.println(getServer() + "-> Chunk " + getLocation().getChunk() + " Sign " + getSign());
            }
            if (getLocation().getChunk() != null && getSign() != null) {
                if (getLocation().getChunk().isLoaded()) {
                    if (Core.DEBUG) {
                        System.out.println("Chunk-Loaded " + getLocation().getChunk().isLoaded());
                        System.out.println("Online: " + si.isOnline());
                    }

                    if (si.isOnline()) {

                        int line = 0;
                        if (Core.DEBUG) {
                            System.out.println("CurrentLines : " + this.lines.values());
                            System.out.println("Size: " + this.lines.values().size());
                        }
                        List<String> liness = new ArrayList<>();
                        for (String s : this.lines.values()) {
                            String set = BungeeSignStringParser.getString(this, line, s);
                            set = ChatColor.translateAlternateColorCodes('&', set);
                            liness.add(set);
                        }

//                    String[] linesa = new String[4];
                        for (int i = 0; i < 4; i++) {
//                        linesa[i] = lines.get(i);
                            getSign().setLine(i, liness.get(i));
                            if (Core.DEBUG) {
                                System.out.println("Set line " + i + " to " + liness.get(i));
                            }
                        }
                        if (getSign() != null) {
                            getSign().update(true);
                        } else {
                            core.getLogger().log(Level.SEVERE, "Error[Real Sign not found] in refreshing Sign -> {0}", this.getServer());
                        }
                    } else {
                        int line = 0;
                        List<String> liness = new ArrayList<>();
                        for (String s : this.lines.values()) {
//                            System.out.println("======");
//                            System.out.println("");
                            String set = BungeeSignStringParser.getString(this, line, s);
//                            System.out.println("");
//                            System.out.println("======");
                            set = ChatColor.translateAlternateColorCodes('&', set);
                            liness.add(set);
                        }
                        for (int i = 0; i < 4; i++) {
                            getSign().setLine(i, liness.get(i));
                        }
                        if (getSign() != null) {
                            getSign().update(true);
                        } else {
                            core.getLogger().log(Level.SEVERE, "Error[Real Sign not found] in refreshing Sign -> {0}", this.getServer());
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            core.getLogger().log(Level.SEVERE, "Error in refreshing Sign -> {0}", this.getServer());
            e.printStackTrace();
        }
    }

    public boolean needsRefresh() {
        for (String s : this.lines.values()) {
            if (s.contains("%cswitch") || s.contains("%cplayers%") || s.contains("%state") || s.contains("%playersgra%")) {
                return true;
            }
        }
        return false;
    }

    public void refreshColor() {
        int line = 0;
        List<String> lines = new ArrayList<>();
        for (String s : this.lines.values()) {
            if (s.contains("%cswitch")) {
                String set = BungeeSignStringParser.getString(this, line, s);
                lines.add(set);
            } else {
                lines.add(s);
            }
            line++;
        }
        String[] linesa = new String[4];
        for (int i = 0; i < 4; i++) {
            linesa[i] = lines.get(i);
        }
        for (Player p : getNearbyPlayers()) {
            p.sendSignChange(getLocation(), linesa);
        }
    }

    public ArrayList<Player> getNearbyPlayers() {
        ArrayList<Player> ret = new ArrayList<Player>();
        Location loc = getLocation();
        int chucnkRadius = core.getConfig().getInt("chunk-radius");
        for (Entity e : getEntitiesInChunks(loc, chucnkRadius)) {
            if (e.getType() == EntityType.PLAYER) {
                ret.add((Player) e);
            }
        }
        return ret;
    }

    public static ArrayList<Entity> getEntitiesInChunks(Location l, int chunkRadius) {
        Block b = l.getBlock();
        ArrayList<Entity> entities = new ArrayList<Entity>();
        for (int x = -16 * chunkRadius; x <= 16 * chunkRadius; x += 16) {
            for (int z = -16 * chunkRadius; z <= 16 * chunkRadius; z += 16) {
                for (Entity e : b.getRelative(x, 0, z).getChunk().getEntities()) {
                    entities.add(e);
                }
            }
        }
        return entities;
    }

    public String extractColor(String raw) {
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public static BungeeSign outOfString(Core bSignsMain, String s) {
        String[] parts = s.split(" ");
        String position = parts[1];
        String[] positions = position.split(",");
        int x = Integer.valueOf(positions[0]);
        int y = Integer.valueOf(positions[1]);
        int z = Integer.valueOf(positions[2]);
        BungeeSign bs = new BungeeSign(bSignsMain, new VirtualLocation(x, y, z, positions[3]));
        bs.setServerInfo(bSignsMain.retrieveServerInfo(parts[0]));
        bs.setLine(0, parts[3].replace("_", " "));
        bs.setLine(1, parts[4].replace("_", " "));
        bs.setLine(2, parts[5].replace("_", " "));
        bs.setLine(3, parts[6].replace("_", " "));
        return bs;
    }

    public ServerInfo getServerInfo() {
        return si;
    }

    public void setServerInfo(ServerInfo si) {
        this.si = si;
    }

    public static class ColorAnimation {

        private ArrayList<String> c = new ArrayList<String>();

        private int switcher = 0;

        public ColorAnimation(String raw) {
            String[] codes = raw.split(",");
            for (String s : codes) {
                c.add(ChatColor.translateAlternateColorCodes('&', s));
            }
        }

        public String getColorWithTick() {
            if (switcher < c.size()) {
                String ret = c.get(switcher);
                switcher++;
                return ret;
            } else {
                switcher = 0;
                String ret = c.get(switcher);
                switcher++;
                return ret;
            }
        }

        public String getColor() {
            if (switcher < c.size()) {
                return c.get(switcher);
            } else {
                return c.get(c.size() - 1);
            }
        }

    }

    public static class StatusDisplay {

        private String onl;
        private String offl;
        private String orcon;

        public StatusDisplay(String orcon, String onl, String offl) {
            this.onl = onl;
            this.offl = offl;
            this.orcon = orcon;
        }

        public String getOriginalContent() {
            return orcon;
        }

        public String getOnline() {
            return onl;
        }

        public String getOffline() {
            return offl;
        }
    }

}
