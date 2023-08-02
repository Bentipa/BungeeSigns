package me.Bentipa.BungeeSignsFree.events;

import me.Bentipa.BungeeSignsFree.ServerInfo;
import me.Bentipa.BungeeSignsFree.pinghelp.ServerPing;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Bentipa(Benjamin) | codebucketdev
 * @year 2016
 *
 */
public class BSSBackSendEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private ServerInfo si;
    private ServerPing t;
    private boolean cancelled;

    public BSSBackSendEvent(ServerInfo si, ServerPing t) {
        setServerInfo(si);
        setServerPing(t);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public ServerInfo getServerInfo() {
        return si;
    }

    public void setServerInfo(ServerInfo si) {
        this.si = si;
    }

    public ServerPing getServerPing() {
        return t;
    }

    public void setServerPing(ServerPing t) {
        this.t = t;
    }

}
