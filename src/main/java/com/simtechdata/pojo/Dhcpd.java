package com.simtechdata.pojo;
import java.util.HashMap;
import java.util.Map;
public class Dhcpd {
    private Opt3 opt3;
    private Lan lan;
    public Opt3 getOpt3() {
        return opt3;
    }
    public void setOpt3(Opt3 opt3) {
        this.opt3 = opt3;
    }
    public Lan getLan() {
        return lan;
    }
    public void setLan(Lan lan) {
        this.lan = lan;
    }
}
