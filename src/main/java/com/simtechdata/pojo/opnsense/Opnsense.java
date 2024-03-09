package com.simtechdata.pojo.opnsense;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.simtechdata.pojo.OPNsense;

@JacksonXmlRootElement(localName = "opnsense")
public class Opnsense {
    @JacksonXmlProperty(localName = "OPNsense")
    private OPNsense OPNsense;
    public OPNsense getOPNsense() {
        return OPNsense;
    }
    public void setOPNsense(OPNsense oPNsense) {
        this.OPNsense = oPNsense;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<opnsense>\n");
        sb.append(OPNsense.toString());
        sb.append("</opnsense>\n");
        return sb.toString();
    }

}
