package com.simtechdata.pojo;
import java.util.HashMap;
import java.util.Map;
public class Staticmap {
    private String ipaddr;
    private String mac;
    private String hostname = "";
    private String descr = "";
    public String getDescr() {
        return descr;
    }
    public void setDescr(String descr) {
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
    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }
    public String getMac() {
        return mac;
    }
    public void setMac(String mac) {
        this.mac = mac;
    }
}
