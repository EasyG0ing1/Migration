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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  <OPNsense>\n");
        sb.append("  ").append(Kea.toString());
        sb.append("  </OPNsense>\n");
        return sb.toString();
    }

}
