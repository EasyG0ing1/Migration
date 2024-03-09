package com.simtechdata.pojo;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Reservation {
    @JacksonXmlProperty(isAttribute = true)
    private String uuid = "";
    private String subnet;
    private String ip_Address;
    private String hw_Address;
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
    public String getHw_Address() {
        return hw_Address;
    }
    public void setHw_Address(String hw_Address) {
        this.hw_Address = hw_Address;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getIp_Address() {
        return ip_Address;
    }
    public void setIp_Address(String ip_Address) {
        this.ip_Address = ip_Address;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("          <reservation uuid=\"").append(uuid).append("\">").append("\n");
        sb.append("            ").append("<subnet>").append(subnet).append("</subnet>").append("\n");
        sb.append("            ").append("<ip_address>").append(ip_Address).append("</ip_address>").append("\n");
        sb.append("            ").append("<hw_address>").append(hw_Address).append("</hw_address>").append("\n");
        if(hostname.isEmpty()) {
            sb.append("            ").append("<hostname/>\n");
        }
        else {
            sb.append("            ").append("<hostname>").append(hostname).append("</hostname>").append("\n");
        }
        if(description.isEmpty()) {
            sb.append("            ").append("<description/>\n");
        }
        else {
            sb.append("            ").append("<description>").append(description).append("</description>").append("\n");
        }
        sb.append("          </reservation>").append("\n");
        return sb.toString();
    }
}
