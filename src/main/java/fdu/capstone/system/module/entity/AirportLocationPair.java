package fdu.capstone.system.module.entity;

import lombok.Data;

@Data
public class AirportLocationPair {
    private String airport;
    private String location;

    public AirportLocationPair(String airport, String location) {
        this.airport = airport;
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAirport() {
        return airport;
    }

    public void setAirport(String airport) {
        this.airport = airport;
    }

    @Override
    public String toString() {
        return "Location: " + location + ", Airport: " + airport;
    }
}
