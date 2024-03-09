package com.simtechdata.pojo;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashMap;
import java.util.Map;
public class Subnet4 {
    private String subnet;
    @JacksonXmlProperty(isAttribute = true)
    private String uuid;
    public String getSubnet() {
        return subnet;
    }
    public void setSubnet(String subnet) {
        this.subnet = subnet;
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
        sb.append("          <subnet4 uuid=\"").append(uuid).append("\">").append("\n");
        sb.append("            ").append("<subnet>").append(subnet).append("/<subnet>").append("\n");
        sb.append("          </subnet4>").append("\n");
        return sb.toString();
    }
}
