package se.kth.id2203.broadcasting;

import se.sics.kompics.network.Address;

/**
 * Created by sindrikaldal on 17/02/17.
 */
public class VectorClockElement {

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
