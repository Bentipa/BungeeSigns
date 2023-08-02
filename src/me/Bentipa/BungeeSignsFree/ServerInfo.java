package me.Bentipa.BungeeSignsFree;

import me.Bentipa.BungeeSignsFree.pinghelp.ServerPing;
import java.net.InetSocketAddress;

import org.bukkit.Bukkit;

public class ServerInfo {

    private ServerPing ping;
    private String name;
    private InetSocketAddress address;
    private int timeout;

    private boolean local;
    private boolean online;
    private int playercount;
    private int maxplayers;
    private String motd;
    private String displayname;
    private String version;
    private String protocol;

    private long pingStartTime;
    private long pingEndTime;

    private int failed_connections = 0;

    public ServerInfo(String servername, String displayname, String address, int port, int timeout) {
        this.ping = new ServerPing();
        this.online = false;
        this.name = servername;
        this.displayname = displayname;
        this.address = new InetSocketAddress(address, port);
        this.timeout = timeout;
        this.pingStartTime = System.currentTimeMillis();
        this.pingEndTime = System.currentTimeMillis();

        if (Bukkit.getServer().getPort() == Integer.valueOf(port) && Bukkit.getServer().getIp().equals(address)) {
            this.local = true;
        }
    }

    public void setFailedConnections(int fcons) {
        if (fcons == 0) {
            this.setOnline(true);
            this.failed_connections = 0;
        } else {
            this.failed_connections = fcons;
            if (this.failed_connections > Core.getInstance().getConfig().getInt("server-max-failed-connections")) {
                this.setOnline(false);
            }
        }
    }

    public int getFailedConnections() {
        return this.failed_connections;
    }

    public ServerPing getPing() {
        return ping;
    }

    public void setPing(ServerPing ping) {
        this.ping = ping;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public boolean isOnline() {
        return this.online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getPlayerCount() {
        return this.playercount;
    }

    public void setPlayerCount(int playercount) {
        this.playercount = playercount;
    }

    public int getMaxPlayers() {
        return this.maxplayers;
    }

    public void setMaxPlayers(int maxplayers) {
        this.maxplayers = maxplayers;
    }

    public String getMotd() {
        return this.motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public String getDisplayname() {
        return this.displayname;
    }

    public void setPingStart(long time) {
        this.pingStartTime = time;
    }

    public void setPingEnd(long time) {
        this.pingEndTime = time;
    }

    public void resetPingDelay() {
        this.pingStartTime = System.currentTimeMillis();
    }
}
