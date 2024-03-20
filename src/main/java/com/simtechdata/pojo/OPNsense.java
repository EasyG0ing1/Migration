package com.simtechdata.pojo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "OPNsense")
public class OPNsense {
    @JacksonXmlProperty(localName = "Kea")
    private Kea Kea;

    public Kea getKea() {
        return Kea;
    }

    public void setKea(Kea kea) {
        this.Kea = kea;
    }
}
