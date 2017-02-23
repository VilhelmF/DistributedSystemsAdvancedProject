package se.kth.id2203.ReadWrite;

public class ReadListValue implements Comparable {
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

    @Override
    public int compareTo(Object o) {
        ReadListValue othervalue = (ReadListValue) o;

        if (this.ts == othervalue.ts) {
            return this.wr > othervalue.wr ? 1 : 0;
        } else {
            return this.ts > othervalue.ts ? 1 : 0;
        }
    }
}
