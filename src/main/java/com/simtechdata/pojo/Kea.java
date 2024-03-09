package com.simtechdata.pojo;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.HashMap;
import java.util.Map;
@JacksonXmlRootElement(localName = "Kea")
public class Kea {
    private Dhcp4 dhcp4;
    public Dhcp4 getDhcp4() {
        return dhcp4;
    }
    public void setDhcp4(Dhcp4 dhcp4) {
        this.dhcp4 = dhcp4;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  <Kea>\n");
        sb.append("    ").append(dhcp4.toString());
        sb.append("    </Kea>\n");
        return sb.toString();
    }
}
