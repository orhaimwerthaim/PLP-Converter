package utils;

public class KeyValuePair<Key,Value> {
    public Key Key;
    public String KeyDesc;
    public Value Value;
    public String ValueDesc;
    public Value getValue()
    {
        return  Value;
    }

    public KeyValuePair()
    {

    }

    public KeyValuePair(Key k, Value v)
    {
        Key = k;
        Value = v;
    }
    @Override
    public String toString() {
        return (Key == null ? "" : Key.toString()) + (Value == null ? "" : Value.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof KeyValuePair ))return false;
        KeyValuePair o = (KeyValuePair)obj;
        return toString().equals(o.toString());
    }
}
