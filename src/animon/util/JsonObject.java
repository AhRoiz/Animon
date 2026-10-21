package animon.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper bertipe di atas {@code Map<String,Object>} hasil {@link JsonParser}.
 * Menyediakan getter dengan validasi tipe dan nilai default.
 */
public final class JsonObject {

    private final Map<String, Object> data;

    public JsonObject(Map<String, Object> data) {
        this.data = data;
    }

    public boolean has(String key) {
        return data.containsKey(key) && data.get(key) != null;
    }

    public Set<String> keys() {
        return Collections.unmodifiableSet(data.keySet());
    }

    public String getString(String key) {
        Object v = wajib(key);
        if (!(v instanceof String)) {
            throw tipeSalah(key, "string");
        }
        return (String) v;
    }

    public String getString(String key, String def) {
        return has(key) ? getString(key) : def;
    }

    public int getInt(String key) {
        Object v = wajib(key);
        if (!(v instanceof Number)) {
            throw tipeSalah(key, "angka");
        }
        return ((Number) v).intValue();
    }

    public int getInt(String key, int def) {
        return has(key) ? getInt(key) : def;
    }

    public double getDouble(String key) {
        Object v = wajib(key);
        if (!(v instanceof Number)) {
            throw tipeSalah(key, "angka");
        }
        return ((Number) v).doubleValue();
    }

    public double getDouble(String key, double def) {
        return has(key) ? getDouble(key) : def;
    }

    public boolean getBoolean(String key, boolean def) {
        if (!has(key)) {
            return def;
        }
        Object v = data.get(key);
        if (!(v instanceof Boolean)) {
            throw tipeSalah(key, "boolean");
        }
        return (Boolean) v;
    }

    @SuppressWarnings("unchecked")
    public JsonObject getObject(String key) {
        Object v = wajib(key);
        if (!(v instanceof Map)) {
            throw tipeSalah(key, "object");
        }
        return new JsonObject((Map<String, Object>) v);
    }

    @SuppressWarnings("unchecked")
    public List<JsonObject> getObjectList(String key) {
        Object v = wajib(key);
        if (!(v instanceof List)) {
            throw tipeSalah(key, "array");
        }
        List<JsonObject> hasil = new ArrayList<>();
        int i = 0;
        for (Object o : (List<Object>) v) {
            if (!(o instanceof Map)) {
                throw new JsonException("Elemen ke-" + i + " pada \"" + key + "\" harus berupa object");
            }
            hasil.add(new JsonObject((Map<String, Object>) o));
            i++;
        }
        return hasil;
    }

    private Object wajib(String key) {
        if (!has(key)) {
            throw new JsonException("Field wajib \"" + key + "\" tidak ditemukan");
        }
        return data.get(key);
    }

    private JsonException tipeSalah(String key, String harapan) {
        return new JsonException("Field \"" + key + "\" harus bertipe " + harapan);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
