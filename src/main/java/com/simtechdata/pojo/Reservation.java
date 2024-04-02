package com.simtechdata.pojo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Reservation {
    @JacksonXmlProperty(isAttribute = true)
    private String uuid = "";
    private String subnet;
    private String ip_address;
    private String hw_address;
    private String hostname = "";
    private String description = "";

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getHw_address() {
        return hw_address;
    }

    public void setHw_address(String hw_address) {
        this.hw_address = hw_address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
