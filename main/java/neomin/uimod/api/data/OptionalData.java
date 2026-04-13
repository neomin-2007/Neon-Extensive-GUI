package neomin.uimod.api.data;

public class OptionalData<T> {

    private final Class<T> type;
    private final T[] values;

    @SafeVarargs
    public OptionalData(Class<T> type, T... values) {
        this.type = type;
        this.values = values;
    }

    public Class<T> getType() {
        return type;
    }

    public T[] getValues() {
        return values;
    }

    public T get(int index) {
        return values[index];
    }

    public int size() {
        return values.length;
    }
}