package animon.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser JSON manual sederhana (recursive descent) tanpa library eksternal.
 *
 * Hasil parsing:
 *   object  -> {@code Map<String, Object>} (urutan key dipertahankan)
 *   array   -> {@code List<Object>}
 *   string  -> {@code String}
 *   angka   -> {@code Long} (bilangan bulat) atau {@code Double} (desimal)
 *   boolean -> {@code Boolean}
 *   null    -> {@code null}
 *
 * Contoh pemakaian:
 * <pre>
 *   JsonObject root = JsonParser.parseObject(Files.readString(path));
 *   for (JsonObject h : root.getObjectList("hewan")) {
 *       String nama = h.getString("nama");
 *       int hp      = h.getObject("stat").getInt("maxHp");
 *   }
 * </pre>
 *
 * Catatan: jika ingin memakai Gson, padanannya kira-kira:
 * <pre>
 *   JsonObject root = com.google.gson.JsonParser.parseString(teks).getAsJsonObject();
 *   HewanDto[] arr  = new Gson().fromJson(root.get("hewan"), HewanDto[].class);
 * </pre>
 */
public final class JsonParser {

    private final String teks;
    private int pos;

    private JsonParser(String teks) {
        this.teks = teks;
    }

    /** Parse JSON apa pun (object/array/nilai primitif). */
    public static Object parse(String json) {
        if (json == null) {
            throw new JsonException("Input JSON null");
        }
        // Buang BOM UTF-8 bila ada (sering muncul di file yang disimpan Notepad).
        String bersih = json.startsWith("\uFEFF") ? json.substring(1) : json;
        JsonParser p = new JsonParser(bersih);
        p.lewatiSpasi();
        Object hasil = p.bacaNilai();
        p.lewatiSpasi();
        if (p.pos < p.teks.length()) {
            throw p.error("Ada karakter berlebih setelah akhir JSON");
        }
        return hasil;
    }

    /** Parse JSON yang root-nya wajib berupa object. */
    @SuppressWarnings("unchecked")
    public static JsonObject parseObject(String json) {
        Object root = parse(json);
        if (!(root instanceof Map)) {
            throw new JsonException("Root JSON harus berupa object { ... }");
        }
        return new JsonObject((Map<String, Object>) root);
    }

    // ------------------------------------------------------------------
    // Recursive descent
    // ------------------------------------------------------------------

    private Object bacaNilai() {
        if (pos >= teks.length()) {
            throw error("JSON berakhir tiba-tiba");
        }
        char c = teks.charAt(pos);
        switch (c) {
            case '{':
                return bacaObject();
            case '[':
                return bacaArray();
            case '"':
                return bacaString();
            case 't':
                harap("true");
                return Boolean.TRUE;
            case 'f':
                harap("false");
                return Boolean.FALSE;
            case 'n':
                harap("null");
                return null;
            default:
                if (c == '-' || Character.isDigit(c)) {
                    return bacaAngka();
                }
                throw error("Karakter tidak terduga '" + c + "'");
        }
    }

    private Map<String, Object> bacaObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++; // lewati '{'
        lewatiSpasi();
        if (cek('}')) {
            pos++;
            return map;
        }
        while (true) {
            lewatiSpasi();
            if (!cek('"')) {
                throw error("Key object harus berupa string");
            }
            String key = bacaString();
            lewatiSpasi();
            konsumsi(':');
            lewatiSpasi();
            if (map.containsKey(key)) {
                throw error("Key duplikat: \"" + key + "\"");
            }
            map.put(key, bacaNilai());
            lewatiSpasi();
            if (cek(',')) {
                pos++;
                continue;
            }
            if (cek('}')) {
                pos++;
                return map;
            }
            throw error("Diharapkan ',' atau '}'");
        }
    }

    private List<Object> bacaArray() {
        List<Object> list = new ArrayList<>();
        pos++; // lewati '['
        lewatiSpasi();
        if (cek(']')) {
            pos++;
            return list;
        }
        while (true) {
            lewatiSpasi();
            list.add(bacaNilai());
            lewatiSpasi();
            if (cek(',')) {
                pos++;
                continue;
            }
            if (cek(']')) {
                pos++;
                return list;
            }
            throw error("Diharapkan ',' atau ']'");
        }
    }

    private String bacaString() {
        pos++; // lewati '"'
        StringBuilder sb = new StringBuilder();
        while (pos < teks.length()) {
            char c = teks.charAt(pos++);
            if (c == '"') {
                return sb.toString();
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (pos >= teks.length()) {
                throw error("Escape tidak lengkap");
            }
            char e = teks.charAt(pos++);
            switch (e) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (pos + 4 > teks.length()) {
                        throw error("Escape \\u tidak lengkap");
                    }
                    try {
                        sb.append((char) Integer.parseInt(teks.substring(pos, pos + 4), 16));
                    } catch (NumberFormatException ex) {
                        throw error("Escape \\u tidak valid");
                    }
                    pos += 4;
                    break;
                default:
                    throw error("Escape tidak valid: \\" + e);
            }
        }
        throw error("String tidak ditutup dengan '\"'");
    }

    private Number bacaAngka() {
        int awal = pos;
        if (cek('-')) {
            pos++;
        }
        while (pos < teks.length() && "0123456789.eE+-".indexOf(teks.charAt(pos)) >= 0) {
            pos++;
        }
        String s = teks.substring(awal, pos);
        try {
            if (s.contains(".") || s.contains("e") || s.contains("E")) {
                return Double.parseDouble(s);
            }
            return Long.parseLong(s);
        } catch (NumberFormatException ex) {
            throw error("Angka tidak valid: " + s);
        }
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private void lewatiSpasi() {
        while (pos < teks.length() && Character.isWhitespace(teks.charAt(pos))) {
            pos++;
        }
    }

    private boolean cek(char c) {
        return pos < teks.length() && teks.charAt(pos) == c;
    }

    private void konsumsi(char c) {
        if (!cek(c)) {
            throw error("Diharapkan '" + c + "'");
        }
        pos++;
    }

    private void harap(String kata) {
        if (!teks.startsWith(kata, pos)) {
            throw error("Diharapkan '" + kata + "'");
        }
        pos += kata.length();
    }

    private JsonException error(String pesan) {
        int baris = 1;
        int kolom = 1;
        for (int i = 0; i < Math.min(pos, teks.length()); i++) {
            if (teks.charAt(i) == '\n') {
                baris++;
                kolom = 1;
            } else {
                kolom++;
            }
        }
        return new JsonException(pesan + " (baris " + baris + ", kolom " + kolom + ")");
    }
}
