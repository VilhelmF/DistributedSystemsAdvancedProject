package se.kth.id2203.broadcasting;

import se.sics.kompics.network.Address;

import java.io.Serializable;

public class VectorClockElement implements Serializable {

    private Address address;
    private int value;

    public VectorClockElement(Address address, int value) {
        this.address = address;
        this.value = value;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
