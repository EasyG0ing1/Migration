package com.simtechdata.pojo;

public class Staticmap {

    public Staticmap(String mac, String ipAddress, String hostname, String description) {
        this.mac = mac;
        this.cid = "";
        this.ipAddress = ipAddress;
        this.hostname = hostname;
        this.description = description;
    }

    public Staticmap(String cid, String ipAddress) {
        this.mac = "";
        this.cid = cid;
        this.ipAddress = ipAddress;
    }

    private final String mac;
    private final String cid;
    private final String ipAddress;
    private String hostname = "";
    private String description = "";

    public void setHostname(String hostname) { this.hostname = hostname; }

    public void setDescription(String descr) { this.description = descr; }

    public String getMac() { return mac; }

    public String getCid() { return cid; }

    public String getIpAddress() { return ipAddress; }

    public String getHostname() { return hostname; }

    public String getDescription() { return description; }
}
