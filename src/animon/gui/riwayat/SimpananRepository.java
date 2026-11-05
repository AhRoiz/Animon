package animon.gui.riwayat;

import animon.battle.BattleEngine;
import animon.data.GameSeeder;
import animon.enums.Elemen;
import animon.enums.StatType;
import animon.enums.Tipe;
import animon.item.Item;
import animon.model.Buff;
import animon.model.Hewan;
import animon.model.Trainer;
import animon.util.JsonObject;
import animon.util.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Menyimpan dan memulihkan sesi pertarungan aktif ke {@code data/savegame.json}
 * hanya melalui API publik {@code GameSeeder}, {@code Trainer}, {@code Hewan}, dan {@code BattleEngine}.
 */
public final class SimpananRepository {

    public static final String FILE_SIMPANAN = "savegame.json";

    /** Hasil pemulihan sesi pertarungan untuk dilanjutkan di GUI. */
    public static final class SesiTersimpan {
        private final String waktuSimpan;
        private final BattleEngine engine;
        private final List<String> logLawan;
        private final List<LangkahReplay> langkahTerekam;

        public SesiTersimpan(String waktuSimpan, BattleEngine engine,
                             List<String> logLawan, List<LangkahReplay> langkahTerekam) {
            this.waktuSimpan = waktuSimpan;
            this.engine = engine;
            this.logLawan = logLawan;
            this.langkahTerekam = langkahTerekam;
        }

        public String getWaktuSimpan()                 { return waktuSimpan; }
        public BattleEngine getEngine()                { return engine; }
        public List<String> getLogLawan()              { return logLawan; }
        public List<LangkahReplay> getLangkahTerekam() { return langkahTerekam; }
    }

    private final Path file;

    public SimpananRepository(Path folderData) {
        this.file = folderData.resolve(FILE_SIMPANAN);
    }

    public boolean adaSimpanan() {
        return Files.isRegularFile(file);
    }

    public String ringkasanSimpanan() {
        if (!adaSimpanan()) return null;
        try {
            JsonObject root = JsonParser.parseObject(Files.readString(file, StandardCharsets.UTF_8));
            String waktu = root.getString("waktu", "-");
            int ronde = root.getInt("ronde", 1);
            String p1 = root.getObject("trainer1").getString("nama");
            String p2 = root.getObject("trainer2").getString("nama");
            return p1 + " vs " + p2 + " (Ronde " + ronde + ", " + waktu + ")";
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    public void hapus() {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
        }
    }

    public void simpan(String waktu, BattleEngine engine,
                       List<String> logLawan, List<LangkahReplay> langkahTerekam) throws IOException {
        Trainer p1 = engine.getPemain(0);
        Trainer p2 = engine.getPemain(1);
        int indeksAktif = engine.getPemainAktif() == p1 ? 0 : 1;

        StringBuilder sb = new StringBuilder("{\n");
        sb.append("  \"waktu\": ").append(JsonWriter.str(waktu)).append(",\n");
        sb.append("  \"ronde\": ").append(engine.getRonde()).append(",\n");
        sb.append("  \"indeksAktif\": ").append(indeksAktif).append(",\n");
        sb.append("  \"logLawanJson\": ").append(JsonWriter.str(JsonWriter.arrayString(logLawan))).append(",\n");
        sb.append("  \"trainer1\": ").append(tulisTrainer(p1)).append(",\n");
        sb.append("  \"trainer2\": ").append(tulisTrainer(p2)).append(",\n");
        sb.append("  \"langkah\": [\n");
        for (int i = 0; i < langkahTerekam.size(); i++) {
            if (i > 0) sb.append(",\n");
            LangkahReplay l = langkahTerekam.get(i);
            sb.append("    { ")
              .append("\"no\": ").append(l.getNomorLangkah()).append(", ")
              .append("\"ronde\": ").append(l.getRonde()).append(", ")
              .append("\"pelakuTrainer\": ").append(JsonWriter.str(l.getPelakuTrainer())).append(", ")
              .append("\"pelakuHewan\": ").append(JsonWriter.str(l.getPelakuHewan())).append(", ")
              .append("\"hpPelakuSebelum\": ").append(l.getHpPelakuSebelum()).append(", ")
              .append("\"hpPelakuSesudah\": ").append(l.getHpPelakuSesudah()).append(", ")
              .append("\"maxHpPelaku\": ").append(l.getMaxHpPelaku()).append(", ")
              .append("\"targetTrainer\": ").append(JsonWriter.str(l.getTargetTrainer())).append(", ")
              .append("\"targetHewan\": ").append(JsonWriter.str(l.getTargetHewan())).append(", ")
              .append("\"hpTargetSebelum\": ").append(l.getHpTargetSebelum()).append(", ")
              .append("\"hpTargetSesudah\": ").append(l.getHpTargetSesudah()).append(", ")
              .append("\"maxHpTarget\": ").append(l.getMaxHpTarget()).append(", ")
              .append("\"aksi\": ").append(JsonWriter.str(l.getRingkasanAksi())).append(", ")
              .append("\"logJson\": ").append(JsonWriter.str(JsonWriter.arrayString(l.getLog())))
              .append(" }");
        }
        sb.append("\n  ]\n}\n");

        if (file.getParent() != null) Files.createDirectories(file.getParent());
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String tulisTrainer(Trainer t) {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("    \"nama\": ").append(JsonWriter.str(t.getNama())).append(",\n");
        sb.append("    \"gold\": ").append(t.getGold()).append(",\n");
        sb.append("    \"kills\": ").append(t.getJumlahKill()).append(",\n");
        sb.append("    \"indexAktif\": ").append(t.getIndexAktif()).append(",\n");
        List<String> idItem = new ArrayList<>();
        for (int i = 0; i < t.jumlahItem(); i++) {
            Item it = t.getItem(i);
            if (it != null) idItem.add(it.getId());
        }
        sb.append("    \"itemsJson\": ").append(JsonWriter.str(JsonWriter.arrayString(idItem))).append(",\n");
        sb.append("    \"tim\": [\n");
        List<Hewan> tim = t.getTim();
        for (int i = 0; i < tim.size(); i++) {
            if (i > 0) sb.append(",\n");
            Hewan h = tim.get(i);
            sb.append("      { ")
              .append("\"id\": ").append(JsonWriter.str(h.getId())).append(", ")
              .append("\"level\": ").append(h.getLevel()).append(", ")
              .append("\"tipe\": ").append(JsonWriter.str(h.getTipe().name())).append(", ")
              .append("\"elemen\": ").append(JsonWriter.str(h.getElemen().name())).append(", ")
              .append("\"maxHp\": ").append(h.getMaxHp()).append(", ")
              .append("\"hp\": ").append(h.getHp()).append(", ")
              .append("\"attack\": ").append(h.getAttack()).append(", ")
              .append("\"defense\": ").append(h.getDefense()).append(", ")
              .append("\"speed\": ").append(h.getSpeed()).append(", ")
              .append("\"shield\": ").append(h.getShield()).append(", ")
              .append("\"defending\": ").append(h.isDefending()).append(", ")
              .append("\"buffs\": [");
            List<Buff> buffs = h.getBuffs();
            for (int b = 0; b < buffs.size(); b++) {
                if (b > 0) sb.append(", ");
                Buff bf = buffs.get(b);
                sb.append("{ \"sumber\": ").append(JsonWriter.str(bf.getSumber()))
                  .append(", \"stat\": ").append(JsonWriter.str(bf.getStat().name()))
                  .append(", \"nilai\": ").append(bf.getNilai())
                  .append(", \"durasi\": ").append(bf.getSisaDurasi()).append(" }");
            }
            sb.append("] }");
        }
        sb.append("\n    ]\n  }");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public SesiTersimpan muat(GameSeeder seeder) throws IOException {
        String teks = Files.readString(file, StandardCharsets.UTF_8);
        JsonObject root = JsonParser.parseObject(teks);
        String waktu = root.getString("waktu", "-");
        int targetRonde = root.getInt("ronde", 1);
        int indeksAktif = root.getInt("indeksAktif", 0);

        JsonObject o1 = root.getObject("trainer1");
        JsonObject o2 = root.getObject("trainer2");
        Trainer p1 = bangunTrainerDasar(seeder, o1);
        Trainer p2 = bangunTrainerDasar(seeder, o2);

        BattleEngine engine = new BattleEngine(p1, p2);
        Trainer targetTrainer = indeksAktif == 0 ? p1 : p2;

        // Majukan ronde & giliran sebelum buff dipasang agar durasi buff tidak berkurang saat pemulihan.
        int pengaman = 0;
        while ((engine.getRonde() < targetRonde || engine.getPemainAktif() != targetTrainer) && pengaman < 400) {
            engine.akhiriGiliran();
            pengaman++;
        }

        pasangStatusLanjutan(p1, o1);
        pasangStatusLanjutan(p2, o2);

        List<String> logLawan = parseStringList(root.getString("logLawanJson", "[]"));
        List<LangkahReplay> langkah = new ArrayList<>();
        if (root.has("langkah")) {
            for (JsonObject l : root.getObjectList("langkah")) {
                langkah.add(new LangkahReplay(
                        l.getInt("no"),
                        l.getInt("ronde"),
                        l.getString("pelakuTrainer"),
                        l.getString("pelakuHewan"),
                        l.getInt("hpPelakuSebelum"),
                        l.getInt("hpPelakuSesudah"),
                        l.getInt("maxHpPelaku"),
                        l.getString("targetTrainer"),
                        l.getString("targetHewan"),
                        l.getInt("hpTargetSebelum"),
                        l.getInt("hpTargetSesudah"),
                        l.getInt("maxHpTarget"),
                        l.getString("aksi"),
                        parseStringList(l.getString("logJson", "[]"))));
            }
        }
        return new SesiTersimpan(waktu, engine, logLawan, langkah);
    }

    private static Trainer bangunTrainerDasar(GameSeeder seeder, JsonObject o) {
        Trainer t = new Trainer(o.getString("nama"), o.getInt("gold", 0));
        int kills = o.getInt("kills", 0);
        for (int i = 0; i < kills; i++) t.catatKill();

        for (String idItem : parseStringList(o.getString("itemsJson", "[]"))) {
            t.tambahItem(seeder.getItem(idItem));
        }

        List<JsonObject> timJson = o.getObjectList("tim");
        int idxAktif = Math.max(0, Math.min(o.getInt("indexAktif", 0), timJson.size() - 1));
        for (JsonObject ho : timJson) {
            Hewan h = seeder.buatHewan(ho.getString("id"));
            int lv = ho.getInt("level", h.getLevel());
            if (lv > h.getLevel()) h.naikLevel(lv - h.getLevel());
            h.ubahTipe(Tipe.fromString(ho.getString("tipe")));
            h.ubahElemen(Elemen.fromString(ho.getString("elemen")));
            int maxHp = ho.getInt("maxHp", h.getMaxHp());
            if (maxHp > h.getMaxHp()) h.tambahMaxHp(maxHp - h.getMaxHp());
            int atk = ho.getInt("attack", h.getAttack());
            if (atk > h.getAttack()) h.tambahAttack(atk - h.getAttack());
            int def = ho.getInt("defense", h.getDefense());
            if (def > h.getDefense()) h.tambahDefense(def - h.getDefense());
            int spd = ho.getInt("speed", h.getSpeed());
            if (spd > h.getSpeed()) h.tambahSpeed(spd - h.getSpeed());
            t.tambahHewan(h);
        }

        for (int i = 0; i < timJson.size(); i++) {
            if (i == idxAktif) continue;
            terapkanHp(t.getTim().get(i), timJson.get(i).getInt("hp", t.getTim().get(i).getMaxHp()));
        }
        if (idxAktif != 0) t.switchHewan(idxAktif);
        terapkanHp(t.getTim().get(idxAktif), timJson.get(idxAktif).getInt("hp", t.getTim().get(idxAktif).getMaxHp()));
        return t;
    }

    private static void terapkanHp(Hewan h, int hpSimpan) {
        if (hpSimpan <= 0) {
            h.terimaDamage(h.getMaxHp() + 9999);
        } else if (hpSimpan < h.getMaxHp()) {
            h.terimaDamage(h.getMaxHp() - hpSimpan);
        }
    }

    private static void pasangStatusLanjutan(Trainer t, JsonObject o) {
        List<JsonObject> timJson = o.getObjectList("tim");
        for (int i = 0; i < timJson.size(); i++) {
            Hewan h = t.getTim().get(i);
            if (h.isFainted()) continue;
            JsonObject ho = timJson.get(i);
            int shield = ho.getInt("shield", 0);
            if (shield > 0) h.pasangShield(shield);
            h.setDefending(ho.getBoolean("defending", false));
            if (ho.has("buffs")) {
                for (JsonObject bo : ho.getObjectList("buffs")) {
                    StatType st = StatType.fromString(bo.getString("stat"));
                    int durasi = Math.max(1, bo.getInt("durasi", 1));
                    if (st == StatType.TAUNT) {
                        h.tambahBuff(Buff.taunt(bo.getString("sumber"), durasi));
                    } else {
                        h.tambahBuff(new Buff(bo.getString("sumber"), st, bo.getInt("nilai"), durasi));
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> parseStringList(String jsonArray) {
        List<String> hasil = new ArrayList<>();
        Object parsed = JsonParser.parse(jsonArray);
        if (parsed instanceof List) {
            for (Object item : (List<Object>) parsed) {
                if (item != null) hasil.add(item.toString());
            }
        }
        return hasil;
    }
}
