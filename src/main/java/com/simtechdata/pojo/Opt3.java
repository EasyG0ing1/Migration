package com.simtechdata.pojo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Opt3 {
    private List<Staticmap> staticmap = new ArrayList<>();
    private String domain;
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
