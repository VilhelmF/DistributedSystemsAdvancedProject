package se.kth.id2203.kvstore;

import com.google.common.base.MoreObjects;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class PutOperation implements KompicsEvent, Serializable {

    private static final long serialVersionUID = 2525600659083087179L;

    public final String key;
    public final String value;
    public final UUID id;

    public PutOperation(String key, String value) {
        this.key = key;
        this.value = value;
        this.id = UUID.randomUUID();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("key", key)
                .add("value", value)
                .toString();
    }
}
