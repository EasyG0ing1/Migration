package com.simtechdata.pojos;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Subnet4 {

    public Subnet4(String subnet, String uuid) {
        this.subnet = subnet;
        this.uuid = uuid;
    }

    private final String subnet;
    @JacksonXmlProperty(isAttribute = true)
    private final String uuid;


    public String getSubnet() {
        return subnet;
    }

    public String getUuid() {
        return uuid;
    }
}
