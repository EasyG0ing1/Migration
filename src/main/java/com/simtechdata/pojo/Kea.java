package com.simtechdata.pojo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "Kea")
public class Kea {
    private Dhcp4 dhcp4;

    public Dhcp4 getDhcp4() {
        return dhcp4;
    }

    public void setDhcp4(Dhcp4 dhcp4) {
        this.dhcp4 = dhcp4;
    }
}
