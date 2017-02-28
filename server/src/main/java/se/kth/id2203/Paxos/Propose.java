package se.kth.id2203.Paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class Propose implements KompicsEvent, Serializable {

    public UUID uuid;
    //Hendum bara í smá ofurgaur með nulls ef það passar ekki. Fixum kannski seinna þegar Paxos er kominn í gang.
    public String method;
    public int key;
    public String value;
    public String reference;

    public Propose(UUID uuid, String method, int key, String value, String reference) {
        this.uuid = uuid;
        this.method = method;
        this.key = key;
        this.value = value;
        this.reference = reference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Propose propose = (Propose) o;

        if (key != propose.key) return false;
        if (uuid != null ? !uuid.equals(propose.uuid) : propose.uuid != null) return false;
        if (method != null ? !method.equals(propose.method) : propose.method != null) return false;
        if (value != null ? !value.equals(propose.value) : propose.value != null) return false;
        return reference != null ? reference.equals(propose.reference) : propose.reference == null;

    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + key;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        return result;
    }

}
