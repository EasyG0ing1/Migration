package com.simtechdata.pojo;

public class Staticmap {

    public Staticmap(String ipaddr, String mac) {
        this.ipaddr = ipaddr;
        this.mac = mac;
    }

    private final String ipaddr;
    private final String mac;
    private String hostname = "";
    private String descr = "";

    public String getDescr() {
        return descr;
    }

    public void setDescription(String descr) {
        this.descr = descr;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpaddr() {
        return ipaddr;
    }

    public String getMac() {
        return mac;
    }
}
