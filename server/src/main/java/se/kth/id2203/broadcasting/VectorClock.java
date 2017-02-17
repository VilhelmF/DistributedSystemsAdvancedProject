package se.kth.id2203.broadcasting;

import se.sics.kompics.network.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by sindrikaldal on 17/02/17.
 */
public class VectorClock implements Comparable {

    List<VectorClockElement> vectorClock;

    public VectorClock() {
        vectorClock = new ArrayList<>();
    }

    public void inc(Address address) {
        for (VectorClockElement element: vectorClock) {
            if (element.getAddress().equals(address)) {
                int value = element.getValue();
                element.setValue(value + 1);
            }
        }
    }

    public void set(Address address, int value) {
        for (VectorClockElement element: vectorClock) {
            if (element.getAddress().equals(address)) {
                element.setValue(value);
                return;
            }
        }
        // If the address wasn't in the list, add it to the vector clock with the value
        vectorClock.add(new VectorClockElement(address, value));
    }

    @Override
    public int compareTo(Object o) {
        VectorClock other = (VectorClock) o;
        for (VectorClockElement element : vectorClock) {
            for (VectorClockElement otherElement : other.vectorClock) {
                if (element.getAddress().equals(otherElement.getAddress())) {
                    if (otherElement.getValue() > element.getValue()) {
                        return 0;
                    }
                }
            }
        }
        return 1;
    }
}
