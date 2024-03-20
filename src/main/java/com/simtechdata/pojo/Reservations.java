package com.simtechdata.pojo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "reservations")
public class Reservations {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "reservation")
    private final List<Reservation> reservation;

    public Reservations(List<Reservation> reservation) {
        this.reservation = reservation;
    }

    public List<Reservation> getReservation() {
        return reservation;
    }
}
