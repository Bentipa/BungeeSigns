package me.Bentipa.BungeeSignsFree;

import lombok.Getter;
import lombok.Setter;
import me.Bentipa.BungeeSignsFree.pinghelp.ServerPing;
import java.net.InetSocketAddress;

import org.bukkit.Bukkit;

public class ServerInfo {

    @Setter
    @Getter
    private ServerPing ping;
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private InetSocketAddress address;
    @Setter
    @Getter
    private int timeout;
    @Setter
    @Getter
    private boolean local;
    @Setter
    @Getter
    private boolean online;
    @Setter
    @Getter
    private int playerCount;
    @Setter
    @Getter
    private int maxPlayers;
    @Setter
    @Getter
    private String motd;
    @Setter
    @Getter
    private String displayName;
    @Setter
    @Getter
    private String version;
    @Setter
    @Getter
    private String protocol;
    @Setter
    @Getter
    private long pingStart;
    @Setter
    @Getter
    private long pingEnd;
    @Getter
    private int failedConnections = 0;

    public ServerInfo(String servername, String displayName, String address, int port, int timeout) {
        this.ping = new ServerPing();
        this.online = false;
        this.name = servername;
        this.displayName = displayName;
        this.address = new InetSocketAddress(address, port);
        this.timeout = timeout;
        this.pingStart = System.currentTimeMillis();
        this.pingEnd = System.currentTimeMillis();

        if (Bukkit.getServer().getPort() == Integer.valueOf(port) && Bukkit.getServer().getIp().equals(address)) {
            this.local = true;
        }
    }

    public void setFailedConnections(int fcons) {
        if (fcons == 0) {
            this.setOnline(true);
            this.failedConnections = 0;
        } else {
            this.failedConnections = fcons;
            if (this.failedConnections > Core.getInstance().getConfig().getInt("server-max-failed-connections")) {
                this.setOnline(false);
            }
        }
    }

    public void resetPingDelay() {
        this.pingStart = System.currentTimeMillis();
    }
}
