package se.kth.id2203.kvstore;

import com.google.common.base.MoreObjects;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

public class CASOperation implements KompicsEvent, Serializable {

    private static final long serialVersionUID = 2525600659083087179L;

    public final String referenceValue;
    public final String key;
    public final String newValue;
    public final UUID id;

    public CASOperation(String referenceValue, String key, String newValue) {
        this.referenceValue = referenceValue;
        this.key = key;
        this.newValue = newValue;
        this.id = UUID.randomUUID();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("referenceValue", referenceValue)
                .add("key", key)
                .add("newValue", newValue)
                .toString();
    }
}
