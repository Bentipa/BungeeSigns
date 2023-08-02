package me.Bentipa.BungeeSignsFree.pinghelp;

import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.Bentipa.BungeeSignsFree.Core;

public class ServerPing {

    private boolean fetching;
    private InetSocketAddress host;
    private int timeout = 7000;
    private Gson gson = new Gson();

    public int getFailedConnections() {
        return failedConnections;
    }

    public void setFailedConnections(int failedConnections) {
        this.failedConnections = failedConnections;
    }

    private int failedConnections = 0;

    public void setAddress(InetSocketAddress host) {
        this.host = host;
        this.fetching = false;
    }

    public InetSocketAddress getAddress() {
        return this.host;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public boolean isFetching() {
        return fetching;
    }

    public void setFetching(boolean pinging) {
        this.fetching = pinging;
    }

    public int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
            if ((k & 0x80) != 128) {
                break;
            }
        }
        return i;
    }

    public void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }

            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    private void writeVarString(DataOutputStream out, String paramString) throws IOException {
        writeVarInt(out, paramString.length());
        out.write(paramString.getBytes(Charset.forName("utf-8")));
    }

    private void sendPacket(DataOutputStream out, byte[] data) throws IOException {
        writeVarInt(out, data.length);
        out.write(data);
    }

    @SuppressWarnings({"resource", "unused"})
    public SResponse fetchData() throws IOException {
        Socket socket = new Socket();
        OutputStream outputStream;
        DataOutputStream dataOutputStream;
        InputStream inputStream;
        InputStreamReader inputStreamReader;
//        socket.setSoTimeout(this.timeout);
        socket.connect(host, timeout);

        outputStream = socket.getOutputStream();
        dataOutputStream = new DataOutputStream(outputStream);

        inputStream = socket.getInputStream();
        inputStreamReader = new InputStreamReader(inputStream);

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream handOut = new DataOutputStream(b);

        handOut.write(new byte[]{0x00});
        writeVarInt(handOut, Core.getInstance().getConfig().getInt("socket-protocol-version", 47));
//        writeVarInt(handOut, 4);
        writeVarInt(handOut, host.getHostString().getBytes(Core.getInstance().getConfig().getString("socket-charset")).length);
        handOut.writeBytes(host.getHostName());
        handOut.writeShort(host.getPort());
        writeVarInt(handOut, 1);

//        dataOutputStream.writeInt(b.size());
        writeVarInt(dataOutputStream, b.size());
        dataOutputStream.write(b.toByteArray());

        dataOutputStream.write(new byte[]{0x01});
        dataOutputStream.write(new byte[]{0x00});

        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int size = readVarInt(dataInputStream); //size of packet
        int id = readVarInt(dataInputStream); //packet id

        if (id == -1) {
//            throw new IOException("Premature end of stream.");
            return null;
        }
        if (id != 0x00) { //we want a status response
//            throw new IOException("Invalid packetID");
            return null;
        }

        int length = readVarInt(dataInputStream); //length of json string
        if (length == -1) {
//            throw new IOException("Premature end of stream.");
            return null;
        }
        if (length < 1) {
//            throw new IOException("Invalid string length.");
            return null;
        }

        byte[] in = new byte[length];
        dataInputStream.readFully(in);  //read json string
        String json = new String(in, Core.getInstance().getConfig().getString("socket-charset"));
//        String json = new String(in, StandardCharsets.UTF_8);

        JSONObject jsonMother = new JSONObject();
        JSONParser parser = new JSONParser();
        try {
            jsonMother = (JSONObject) parser.parse(json);
        } catch (ParseException ex) {
            Logger.getLogger(ServerPing.class.getName()).log(Level.SEVERE, null, ex);
        }
        JSONObject jsonVersion = (JSONObject) jsonMother.get("version");
        String version = (String) jsonVersion.get("name");

        SResponse ret = new SResponse();
        if (Core.DEBUG) {
            System.out.println("ServerPing| Version: " + version + "");
            System.out.println("JSON: \n\r" + json);
        }
        String versionLabel = version.split(" ")[0];
        String[] versionString = version.split(" ")[1].split("\\.");
        int majorVersion = Integer.valueOf(versionString[0]);
        int minorVersion = Integer.valueOf(versionString[1]);
        int releaseVersion = versionString.length == 3 ? Integer.valueOf(versionString[2]) : 0;

        if (version.contains("1.9")) {
            StatusResponse_19 res = gson.fromJson(json, StatusResponse_19.class);
            ret.description = res.getDescription();
            ret.favicon = res.getFavicon();
            ret.players = res.getPlayers().getOnline();
            ret.slots = res.getPlayers().getMax();
            ret.time = res.getTime();
            ret.protocol = res.getVersion().getProtocol();
            ret.version = res.getVersion().getName();
        } else if (version.contains("1.10") || version.contains("1.11") || version.contains("1.12")) {
            StatusResponse_110 res = gson.fromJson(json, StatusResponse_110.class);
            ret.description = res.getDescription();
            ret.players = res.getPlayers().getOnline();
            ret.slots = res.getPlayers().getMax();
            ret.time = res.getTime();
            ret.protocol = res.getVersion().getProtocol();
            ret.version = res.getVersion().getName();
        } else if (majorVersion == 1 && minorVersion > 12 && minorVersion < 16) {
            StatusResponse_113 res = gson.fromJson(json, StatusResponse_113.class);
            ret.description = res.getDescription().text;
            ret.players = res.getPlayers().online;
            ret.slots = res.getPlayers().max;
            ret.time = -1;
            ret.protocol = res.getVersion().protocol;
            ret.version = res.getVersion().name;
        } else if (majorVersion == 1 && minorVersion >= 16) {
            StatusResponse_116 res = gson.fromJson(json, StatusResponse_116.class);
            StringBuilder descriptionExtras = new StringBuilder();
            if (res.getDescription().extra != null) {
                for (StatusResponse_116.DescriptionExtra extra : res.getDescription().extra) {
                    descriptionExtras.append(extra.text);
                    descriptionExtras.append(" ");
                }
                descriptionExtras.deleteCharAt(descriptionExtras.length() - 1);
            }
            ret.description = res.getDescription().text + descriptionExtras.toString();
            ret.players = res.getPlayers().online;
            ret.slots = res.getPlayers().max;
            ret.time = -1;
            ret.protocol = res.getVersion().protocol;
            ret.version = res.getVersion().name;
        } else {
            StatusResponse res = gson.fromJson(json, StatusResponse.class);
            ret.description = res.getDescription();
            ret.favicon = res.getFavicon();
            ret.players = res.getPlayers().getOnline();
            ret.slots = res.getPlayers().getMax();
            ret.time = res.getTime();
            ret.protocol = res.getVersion().getProtocol();
            ret.version = res.getVersion().getName();
        }

        dataOutputStream.close();
        outputStream.close();
        inputStreamReader.close();
        inputStream.close();
        socket.close();

        return ret;
    }

    public class SResponse {

        public String version;
        public String protocol;
        public String favicon;
        public String description;
        public int players;
        public int slots;
        public int time;
    }

}
