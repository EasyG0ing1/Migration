package com.simtechdata.pojo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Reservations {
    private List<Reservation> reservation = new ArrayList<Reservation>();
    public List<Reservation> getReservation() {
        return reservation;
    }
    public void setReservation(List<Reservation> reservation) {
        this.reservation = reservation;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("        <reservations>\n");
        for(Reservation rev : reservation) {
            sb.append(rev.toString());
        }
        sb.append("        </reservations>\n");
        return sb.toString();
    }

}
