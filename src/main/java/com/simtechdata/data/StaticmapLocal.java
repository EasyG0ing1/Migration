package com.simtechdata.data;

public class StaticmapLocal {

    public StaticmapLocal(String mac, String ipAddress, String cid, String hostname, String description, String networkName) {
        this.mac = mac;
        this.ipAddress = ipAddress;
        this.cid = cid;
        this.hostname = hostname;
        this.description = description;
        this.networkName = networkName;
        this.failed = this.mac.isEmpty() || this.ipAddress.isEmpty();
    }

    private final String mac;
    private final String cid;
    private final String ipAddress;
    private final String networkName;
    private final String hostname;
    private final String description;
    private final boolean failed;
    private final static String NL = System.getProperty("line.separator");

    public String getMac() {
        return mac;
    }

    public String getCid() {
        return cid;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public String getDescription() {
        return description;
    }

    public String getNetworkName() {
        return networkName;
    }

    public boolean isFailed() {
        return failed;
    }
}
