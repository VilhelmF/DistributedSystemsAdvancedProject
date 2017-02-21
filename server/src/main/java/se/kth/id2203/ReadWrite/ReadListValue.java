package se.kth.id2203.ReadWrite;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class ReadListValue {
    private int ts;
    private int wr;
    private Object value;

    public ReadListValue(int ts, int wr, Object value) {
        this.ts = ts;
        this.wr = wr;
        this.value = value;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public int getWr() {
        return wr;
    }

    public void setWr(int wr) {
        this.wr = wr;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
