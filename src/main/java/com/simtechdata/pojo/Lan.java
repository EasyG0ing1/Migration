package com.simtechdata.pojo;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Lan {
    private List<Staticmap> staticmap = new ArrayList<>();
    private String domain;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "staticmap")
    public List<Staticmap> getStaticmap() {
        return staticmap;
    }
    public void setStaticmap(List<Staticmap> staticmap) {
        this.staticmap = staticmap;
    }
    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }
}
