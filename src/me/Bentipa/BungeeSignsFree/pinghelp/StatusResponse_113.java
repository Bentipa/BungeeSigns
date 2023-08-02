package me.Bentipa.BungeeSignsFree.pinghelp;
/**
 * Created by Benjamin on 04.09.2018.
 */
public class StatusResponse_113 {

    private Description description;
    private Players players;
    private Version version;

    class Version {
        String name;
        String protocol;
    }

    class Players {
        int max, online;
    }

    class Description {
        String text;
    }

    public Description getDescription() {
        return description;
    }

    public Players getPlayers() {
        return players;
    }

    public Version getVersion() {
        return version;
    }
}
