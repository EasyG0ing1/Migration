package com.simtechdata.pojo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Subnets {
    private List<Subnet4> subnet4 = new ArrayList<Subnet4>();
    public List<Subnet4> getSubnet4() {
        return subnet4;
    }
    public void setSubnet4(List<Subnet4> subnet4) {
        this.subnet4 = subnet4;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("        <subnets>").append("\n");
        for (Subnet4 subnet4 : getSubnet4()) {
            sb.append(subnet4);
        }
        sb.append("        </subnets>\n");
        return sb.toString();
    }
}
