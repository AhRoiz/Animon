package animon.data;

import animon.enums.Elemen;
import animon.enums.JenisItem;
import animon.enums.Role;
import animon.enums.SkillMode;
import animon.enums.StatType;
import animon.enums.Tipe;
import animon.item.Item;
import animon.item.ItemBuff;
import animon.item.ItemEvolusi;
import animon.item.ItemHeal;
import animon.model.Hewan;
import animon.model.HewanDPS;
import animon.model.HewanSupport;
import animon.model.HewanTank;
import animon.model.Skill;
import animon.model.Stat;
import animon.util.JsonException;
import animon.util.JsonObject;
import animon.util.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory / Seeder data game. SATU-SATUNYA tempat objek {@link Hewan} dan {@link Item} dibuat
 * dari file eksternal JSON (tidak ada hardcode hewan di Main).
 *
 * <ul>
 *   <li>Hewan disimpan sebagai "cetakan" (JsonObject). {@link #buatHewan(String)} selalu membuat
 *       instance BARU, sehingga dua trainer tidak berbagi HP/buff yang sama.</li>
 *   <li>Item immutable, jadi cukup dibuat sekali dan dipakai bersama sebagai katalog.</li>
 *   <li>Semua data divalidasi saat load; error menyertakan nama file dan id entri.</li>
 * </ul>
 *
 * Contoh parsing (parser manual {@link JsonParser}, tanpa library eksternal):
 * <pre>
 *   JsonObject root = JsonParser.parseObject(Files.readString(path));
 *   for (JsonObject h : root.getObjectList("hewan")) {
 *       Hewan hewan = GameSeeder.parseHewan(h);   // role menentukan child class
 *   }
 * </pre>
 */
public final class GameSeeder {

    public static final String FILE_HEWAN = "data_hewan.json";
    public static final String FILE_ITEM = "data_item.json";
    public static final int MIN_HEWAN = 2;

    private final Map<String, JsonObject> cetakanHewan = new LinkedHashMap<>();
    private final Map<String, Item> katalogItem = new LinkedHashMap<>();

    /** Membaca {@value #FILE_HEWAN} dan {@value #FILE_ITEM} dari folder data. */
    public static GameSeeder dariFolder(Path folder) throws IOException {
        Path fHewan = folder.resolve(FILE_HEWAN);
        Path fItem = folder.resolve(FILE_ITEM);
        return new GameSeeder(Files.readString(fHewan, StandardCharsets.UTF_8),
                Files.readString(fItem, StandardCharsets.UTF_8));
    }

    public GameSeeder(String jsonHewan, String jsonItem) {
        muatHewan(jsonHewan);
        muatItem(jsonItem);
    }

    // =================================================================== load

    private void muatHewan(String json) {
        List<JsonObject> daftar = bungkus(FILE_HEWAN, null, () -> JsonParser.parseObject(json).getObjectList("hewan"));
        for (int i = 0; i < daftar.size(); i++) {
            JsonObject o = daftar.get(i);
            String label = "hewan ke-" + (i + 1);
            String id = bungkus(FILE_HEWAN, label, () -> o.getString("id"));
            String konteks = label + " (" + id + ")";
            if (cetakanHewan.containsKey(id)) throw gagal(FILE_HEWAN, konteks, "id duplikat");
            bungkus(FILE_HEWAN, konteks, () -> parseHewan(o)); // validasi penuh sekali di awal
            cetakanHewan.put(id, o);
        }
        if (cetakanHewan.size() < MIN_HEWAN) {
            throw gagal(FILE_HEWAN, null, "minimal " + MIN_HEWAN + " hewan diperlukan");
        }
    }

    private void muatItem(String json) {
        List<JsonObject> daftar = bungkus(FILE_ITEM, null, () -> JsonParser.parseObject(json).getObjectList("items"));
        for (int i = 0; i < daftar.size(); i++) {
            JsonObject o = daftar.get(i);
            String label = "item ke-" + (i + 1);
            String id = bungkus(FILE_ITEM, label, () -> o.getString("id"));
            String konteks = label + " (" + id + ")";
            if (katalogItem.containsKey(id)) throw gagal(FILE_ITEM, konteks, "id duplikat");
            katalogItem.put(id, bungkus(FILE_ITEM, konteks, () -> parseItem(o)));
        }
        if (katalogItem.isEmpty()) throw gagal(FILE_ITEM, null, "katalog item kosong");
    }

    // =================================================================== factory hewan

    /**
     * Membuat Hewan dari satu object JSON. Field {@code role} menentukan child class
     * (Polymorphism: pemanggil hanya menerima tipe abstrak {@link Hewan}).
     */
    public static Hewan parseHewan(JsonObject o) {
        String id = o.getString("id");
        String nama = o.getString("nama");
        Role role = Role.fromString(o.getString("role"));
        Tipe tipe = Tipe.fromString(o.getString("tipe"));
        Elemen elemen = Elemen.fromString(o.getString("elemen"));
        int level = o.getInt("level", 5);
        String deskripsi = o.getString("deskripsi", "");

        JsonObject s = o.getObject("stat");
        Stat stat = new Stat(s.getInt("maxHp"), s.getInt("maxMana"), s.getInt("attack"),
                s.getInt("defense"), s.getInt("speed"), s.getInt("evasion", 0));

        JsonObject k = o.getObject("skill");
        Skill skill = new Skill(k.getString("nama"), SkillMode.fromString(k.getString("mode")),
                k.getInt("manaCost"), k.getDouble("power", 0.0), k.getInt("nilai", 0), k.getString("deskripsi", ""));

        switch (role) {
            case DPS:     return new HewanDPS(id, nama, tipe, elemen, level, stat, skill, deskripsi);
            case SUPPORT: return new HewanSupport(id, nama, tipe, elemen, level, stat, skill, deskripsi);
            case TANK:    return new HewanTank(id, nama, tipe, elemen, level, stat, skill, deskripsi);
            default:      throw new IllegalArgumentException("Role belum didukung: " + role);
        }
    }

    // =================================================================== factory item

    /** Membuat Item dari satu object JSON. Field {@code jenis} menentukan child class. */
    public static Item parseItem(JsonObject o) {
        String id = o.getString("id");
        String nama = o.getString("nama");
        int harga = o.getInt("hargaBeli");
        String desk = o.getString("deskripsi", "");
        JenisItem jenis = JenisItem.fromString(o.getString("jenis"));

        switch (jenis) {
            case HEAL:
                return new ItemHeal(id, nama, harga, desk,
                        o.getInt("hp", 0), o.getInt("mana", 0),
                        o.getBoolean("revive", false), o.getInt("revivePersen", 50));
            case BUFF:
                return new ItemBuff(id, nama, harga, desk,
                        StatType.fromString(o.getString("stat")), o.getInt("nilai"),
                        o.getInt("durasi", 2), o.getInt("delayTurn", 0));
            case EVOLUSI:
                return new ItemEvolusi(id, nama, harga, desk,
                        o.has("elemen") ? Elemen.fromString(o.getString("elemen")) : null,
                        o.has("tipe") ? Tipe.fromString(o.getString("tipe")) : null,
                        o.getInt("naikLevel", 0), o.getInt("bonusMaxHp", 0), o.getInt("bonusAtk", 0),
                        o.getInt("bonusDef", 0), o.getInt("bonusSpeed", 0));
            default:
                throw new IllegalArgumentException("Jenis item belum didukung: " + jenis);
        }
    }

    // =================================================================== akses publik

    /** Instance BARU dari hewan dengan id tertentu. */
    public Hewan buatHewan(String id) {
        JsonObject o = cetakanHewan.get(id);
        if (o == null) throw new IllegalArgumentException("Hewan dengan id '" + id + "' tidak ada");
        return parseHewan(o);
    }

    /** Instance baru dari setiap hewan di database (urutan sesuai file). */
    public List<Hewan> buatSemuaHewan() {
        List<Hewan> hasil = new ArrayList<>();
        for (String id : cetakanHewan.keySet()) hasil.add(buatHewan(id));
        return hasil;
    }

    public List<String> getIdHewan() {
        return new ArrayList<>(cetakanHewan.keySet());
    }

    public int getJumlahHewan() {
        return cetakanHewan.size();
    }

    public Item getItem(String id) {
        Item item = katalogItem.get(id);
        if (item == null) throw new IllegalArgumentException("Item dengan id '" + id + "' tidak ada");
        return item;
    }

    public List<Item> getSemuaItem() {
        return Collections.unmodifiableList(new ArrayList<>(katalogItem.values()));
    }

    // =================================================================== error handling

    private interface Langkah<T> {
        T jalankan();
    }

    /** Menjalankan langkah parsing dan membungkus error dengan konteks file &amp; entri. */
    private static <T> T bungkus(String file, String konteks, Langkah<T> langkah) {
        try {
            return langkah.jalankan();
        } catch (JsonException | IllegalArgumentException | IllegalStateException e) {
            throw gagal(file, konteks, e.getMessage());
        }
    }

    private static JsonException gagal(String file, String konteks, String pesan) {
        return new JsonException(file + (konteks == null ? "" : ", " + konteks) + ": " + pesan);
    }
}
