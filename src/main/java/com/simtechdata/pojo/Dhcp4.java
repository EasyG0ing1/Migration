package com.simtechdata.pojo;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.HashMap;
import java.util.Map;
public class Dhcp4 {
    private Reservations reservations;
    private Subnets subnets;
    @JacksonXmlProperty(isAttribute = true)
    private String version = "1.0.0";
    public Reservations getReservations() {
        return reservations;
    }
    public void setReservations(Reservations reservations) {
        this.reservations = reservations;
    }
    public Subnets getSubnets() {
        return subnets;
    }
    public void setSubnets(Subnets subnets) {
        this.subnets = subnets;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  <dhcp4 version=\"1.0.0\">\n");
        sb.append(subnets.toString());
        sb.append(reservations.toString());
        sb.append("      </dhcp4>\n");
        return sb.toString();
    }
}
