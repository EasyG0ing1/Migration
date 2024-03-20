package com.simtechdata.pojo;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

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
}
