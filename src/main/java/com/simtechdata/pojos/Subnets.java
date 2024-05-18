package com.simtechdata.pojos;

import java.util.ArrayList;
import java.util.List;

public class Subnets {
    private List<Subnet4> subnet4 = new ArrayList<>();

    public List<Subnet4> getSubnet4() {
        return subnet4;
    }

    public void setSubnet4(List<Subnet4> subnet4) {
        this.subnet4 = subnet4;
    }
}
