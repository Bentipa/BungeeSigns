package me.Bentipa.BungeeSignsFree.sheduler;

import java.util.List;

import me.Bentipa.BungeeSignsFree.BungeeSign;
import me.Bentipa.BungeeSignsFree.Core;
import me.Bentipa.BungeeSignsFree.events.BSSUpdateEvent;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BungeeSignsRefresher implements Runnable, Listener {

    private me.Bentipa.BungeeSignsFree.Core core;

    int taskid = 0;

    public BungeeSignsRefresher(me.Bentipa.BungeeSignsFree.Core bSignsMain) {
        this.core = bSignsMain;
        bSignsMain.getServer().getPluginManager().registerEvents(this, bSignsMain);
    }

    public long getDelayTime() {
        int inms = core.getConfig().getInt("sign-refresh");
        double d = inms / 1000;
        long inticks = (long) d * 20;
        return inticks;
    }

    public void start() {
//		System.out.println("Created task with delaytime: " + getDelayTime());
        if (taskid != 0) {
            core.getServer().getScheduler().cancelTask(taskid);
        }

        taskid = core.getServer().getScheduler().scheduleSyncRepeatingTask(core, new Runnable() {

            @Override
            public void run() {
                refreshAll();
            }
        }, 0L, getDelayTime());
    }

    @Override
    public void run() {
        final List<BungeeSign> signs = core.getSigns();
        BSSUpdateEvent event = new BSSUpdateEvent(signs);
        Bukkit.getPluginManager().callEvent(event);
        Bukkit.getScheduler().runTaskLater(core, this, getDelayTime());
    }

    @EventHandler
    public void onEvent(BSSUpdateEvent event) {
        if (!event.isCancelled()) {

            for (BungeeSign sign : event.getSigns()) {
                if (sign != null) {
                    sign.refresh();
                }
            }
        }
    }

    public void stop() {
        System.out.println("killed task");
        if (taskid != 0) {
            core.getServer().getScheduler().cancelTask(taskid);
        }
    }

    public void refreshColors() {
        for (BungeeSign bs : core.getSigns()) {
            bs.refreshColor();
        }
    }

    private void refreshAll() {
        try {
            for (BungeeSign bs : core.getSigns()) {
                if (Core.DEBUG) {
                    System.out.println("Refreshing " + bs.getServer() + " -> " + bs.needsRefresh());
                }
                if (bs.needsRefresh()) {
                    bs.refresh();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
