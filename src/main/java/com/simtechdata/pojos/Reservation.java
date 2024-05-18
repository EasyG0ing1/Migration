package com.simtechdata.pojos;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Reservation {
    @JacksonXmlProperty(isAttribute = true)
    private String uuid;
    private String subnet;
    private String ip_address;
    private String hw_address;
    private String hostname;
    private String description;

    public Reservation(String uuid, String subnet, String ip_address, String hw_address, String hostname, String description) {
        this.uuid = uuid;
        this.subnet = subnet;
        this.ip_address = ip_address;
        this.hw_address = hw_address;
        this.hostname = hostname;
        this.description = description;
    }

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

    public Element getReservation(Document doc) {
        Element newReservation = doc.createElement("reservation");
        newReservation.setAttribute("uuid", this.uuid);

        Element subnet = doc.createElement("subnet");
        subnet.setTextContent(this.subnet);
        newReservation.appendChild(subnet);

        Element ipAddress = doc.createElement("ip_address");
        ipAddress.setTextContent(this.ip_address);
        newReservation.appendChild(ipAddress);

        Element hwAddress = doc.createElement("hw_address");
        hwAddress.setTextContent(this.hw_address);
        newReservation.appendChild(hwAddress);

        Element hostname = doc.createElement("hostname");
        hostname.setTextContent(this.hostname);
        newReservation.appendChild(hostname);

        Element description = doc.createElement("description");
        description.setTextContent(this.description);
        newReservation.appendChild(description);

        return newReservation;
    }
}
