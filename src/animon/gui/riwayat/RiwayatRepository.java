package animon.gui.riwayat;

import animon.util.JsonObject;
import animon.util.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Penyimpanan riwayat pertandingan yang mengutamakan MySQL via JDBC bila server aktif.
 * Selalu menulis salinan arsip JSON di folder data agar replay tetap dapat dibuka saat offline.
 */
public final class RiwayatRepository {

    public static final String FILE_RIWAYAT = "riwayat_pertandingan.json";

    private final Path fileLokal;
    private String url;
    private String user;
    private String password;
    private String infoSumber = "Belum diperiksa";
    private boolean mysqlAktif;

    public RiwayatRepository(Path folderData) {
        this.fileLokal = folderData.resolve(FILE_RIWAYAT);
        muatKonfigurasi(folderData);
    }

    private void muatKonfigurasi(Path folderData) {
        Properties prop = new Properties();
        Path fProp = folderData.resolve("db.properties");
        if (Files.isRegularFile(fProp)) {
            try (InputStream in = Files.newInputStream(fProp)) {
                prop.load(in);
            } catch (IOException ignored) {
            }
        }
        url = pilih(System.getenv("ANIMON_DB_URL"), prop.getProperty("db.url"),
                "jdbc:mysql://localhost:3306/animon_db?createDatabaseIfNotExist=true&serverTimezone=UTC");
        user = pilih(System.getenv("ANIMON_DB_USER"), prop.getProperty("db.user"), "root");
        password = pilih(System.getenv("ANIMON_DB_PASS"), prop.getProperty("db.password"), "");
    }

    private static String pilih(String a, String b, String cadangan) {
        if (a != null && !a.isBlank()) return a.trim();
        if (b != null && !b.isBlank()) return b.trim();
        return cadangan;
    }

    public String getInfoSumber() {
        return infoSumber;
    }

    public boolean isMysqlAktif() {
        return mysqlAktif;
    }

    /** Simpan hasil pertandingan ke arsip lokal dan MySQL (jika terhubung). */
    public void simpan(RekamanPertandingan rekaman) {
        List<RekamanPertandingan> lokal = bacaLokalAman();
        long idBaru = rekaman.getId() > 0 ? rekaman.getId() : (lokal.size() + 1L);
        RekamanPertandingan denganId = new RekamanPertandingan(
                idBaru, rekaman.getWaktu(), rekaman.getTrainer1(), rekaman.getTrainer2(),
                rekaman.getPemenang(), rekaman.isMenyerah(), rekaman.getTotalRonde(),
                rekaman.getRingkasanTim1(), rekaman.getRingkasanTim2(), rekaman.getLangkahList());

        simpanKeMysql(denganId);
        lokal.add(0, denganId);
        tulisLokal(lokal);
    }

    /** Muat daftar pertandingan (MySQL diutamakan, jatuh ke file JSON lokal bila MySQL tidak aktif). */
    public List<RekamanPertandingan> muatSemua() throws IOException {
        List<RekamanPertandingan> dariDb = muatDariMysql();
        if (dariDb != null) return dariDb;
        return bacaLokal();
    }

    private Connection bukaKoneksi() throws SQLException {
        DriverManager.setLoginTimeout(2);
        Connection conn = DriverManager.getConnection(url, user, password);
        pastikanSkema(conn);
        return conn;
    }

    private void pastikanSkema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS pertandingan ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "waktu VARCHAR(40) NOT NULL,"
                    + "trainer1 VARCHAR(80) NOT NULL,"
                    + "trainer2 VARCHAR(80) NOT NULL,"
                    + "pemenang VARCHAR(80) NOT NULL,"
                    + "menyerah TINYINT(1) NOT NULL,"
                    + "total_ronde INT NOT NULL,"
                    + "tim1 VARCHAR(255) NOT NULL,"
                    + "tim2 VARCHAR(255) NOT NULL"
                    + ")");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS langkah_replay ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "pertandingan_id BIGINT NOT NULL,"
                    + "nomor_langkah INT NOT NULL,"
                    + "ronde INT NOT NULL,"
                    + "pelaku_trainer VARCHAR(80) NOT NULL,"
                    + "pelaku_hewan VARCHAR(80) NOT NULL,"
                    + "hp_pelaku_sebelum INT NOT NULL,"
                    + "hp_pelaku_sesudah INT NOT NULL,"
                    + "max_hp_pelaku INT NOT NULL,"
                    + "target_trainer VARCHAR(80) NOT NULL,"
                    + "target_hewan VARCHAR(80) NOT NULL,"
                    + "hp_target_sebelum INT NOT NULL,"
                    + "hp_target_sesudah INT NOT NULL,"
                    + "max_hp_target INT NOT NULL,"
                    + "ringkasan_aksi VARCHAR(160) NOT NULL,"
                    + "log_teks TEXT NOT NULL,"
                    + "FOREIGN KEY (pertandingan_id) REFERENCES pertandingan(id) ON DELETE CASCADE"
                    + ")");
        }
    }

    private void simpanKeMysql(RekamanPertandingan r) {
        try (Connection conn = bukaKoneksi()) {
            conn.setAutoCommit(false);
            long matchId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pertandingan (waktu, trainer1, trainer2, pemenang, menyerah, total_ronde, tim1, tim2) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, r.getWaktu());
                ps.setString(2, r.getTrainer1());
                ps.setString(3, r.getTrainer2());
                ps.setString(4, r.getPemenang());
                ps.setInt(5, r.isMenyerah() ? 1 : 0);
                ps.setInt(6, r.getTotalRonde());
                ps.setString(7, r.getRingkasanTim1());
                ps.setString(8, r.getRingkasanTim2());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    matchId = keys.next() ? keys.getLong(1) : r.getId();
                }
            }
            try (PreparedStatement pl = conn.prepareStatement(
                    "INSERT INTO langkah_replay (pertandingan_id, nomor_langkah, ronde, pelaku_trainer, pelaku_hewan, "
                            + "hp_pelaku_sebelum, hp_pelaku_sesudah, max_hp_pelaku, target_trainer, target_hewan, "
                            + "hp_target_sebelum, hp_target_sesudah, max_hp_target, ringkasan_aksi, log_teks) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                for (LangkahReplay l : r.getLangkahList()) {
                    pl.setLong(1, matchId);
                    pl.setInt(2, l.getNomorLangkah());
                    pl.setInt(3, l.getRonde());
                    pl.setString(4, l.getPelakuTrainer());
                    pl.setString(5, l.getPelakuHewan());
                    pl.setInt(6, l.getHpPelakuSebelum());
                    pl.setInt(7, l.getHpPelakuSesudah());
                    pl.setInt(8, l.getMaxHpPelaku());
                    pl.setString(9, l.getTargetTrainer());
                    pl.setString(10, l.getTargetHewan());
                    pl.setInt(11, l.getHpTargetSebelum());
                    pl.setInt(12, l.getHpTargetSesudah());
                    pl.setInt(13, l.getMaxHpTarget());
                    pl.setString(14, l.getRingkasanAksi());
                    pl.setString(15, String.join("\n", l.getLog()));
                    pl.addBatch();
                }
                pl.executeBatch();
            }
            conn.commit();
            mysqlAktif = true;
            infoSumber = "MySQL terhubung";
        } catch (SQLException e) {
            mysqlAktif = false;
            infoSumber = "Arsip lokal JSON (MySQL belum aktif)";
        }
    }

    private List<RekamanPertandingan> muatDariMysql() {
        try (Connection conn = bukaKoneksi();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, waktu, trainer1, trainer2, pemenang, menyerah, total_ronde, tim1, tim2 "
                             + "FROM pertandingan ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            List<RekamanPertandingan> hasil = new ArrayList<>();
            while (rs.next()) {
                long id = rs.getLong("id");
                List<LangkahReplay> langkah = muatLangkahMysql(conn, id);
                hasil.add(new RekamanPertandingan(
                        id,
                        rs.getString("waktu"),
                        rs.getString("trainer1"),
                        rs.getString("trainer2"),
                        rs.getString("pemenang"),
                        rs.getInt("menyerah") == 1,
                        rs.getInt("total_ronde"),
                        rs.getString("tim1"),
                        rs.getString("tim2"),
                        langkah));
            }
            mysqlAktif = true;
            infoSumber = "MySQL terhubung (" + hasil.size() + " laga)";
            return hasil;
        } catch (SQLException e) {
            mysqlAktif = false;
            infoSumber = "Arsip lokal data/" + FILE_RIWAYAT + " (MySQL tidak terhubung)";
            return null;
        }
    }

    private List<LangkahReplay> muatLangkahMysql(Connection conn, long matchId) throws SQLException {
        List<LangkahReplay> hasil = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM langkah_replay WHERE pertandingan_id = ? ORDER BY nomor_langkah ASC")) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String teks = rs.getString("log_teks");
                    List<String> log = (teks == null || teks.isEmpty())
                            ? List.of() : Arrays.asList(teks.split("\\R"));
                    hasil.add(new LangkahReplay(
                            rs.getInt("nomor_langkah"),
                            rs.getInt("ronde"),
                            rs.getString("pelaku_trainer"),
                            rs.getString("pelaku_hewan"),
                            rs.getInt("hp_pelaku_sebelum"),
                            rs.getInt("hp_pelaku_sesudah"),
                            rs.getInt("max_hp_pelaku"),
                            rs.getString("target_trainer"),
                            rs.getString("target_hewan"),
                            rs.getInt("hp_target_sebelum"),
                            rs.getInt("hp_target_sesudah"),
                            rs.getInt("max_hp_target"),
                            rs.getString("ringkasan_aksi"),
                            log));
                }
            }
        }
        return hasil;
    }

    private List<RekamanPertandingan> bacaLokalAman() {
        try {
            return bacaLokal();
        } catch (IOException | RuntimeException e) {
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    private List<RekamanPertandingan> bacaLokal() throws IOException {
        if (!Files.isRegularFile(fileLokal)) return new ArrayList<>();
        String json = Files.readString(fileLokal, StandardCharsets.UTF_8);
        JsonObject root = JsonParser.parseObject(json);
        List<RekamanPertandingan> hasil = new ArrayList<>();
        for (JsonObject m : root.getObjectList("pertandingan")) {
            List<LangkahReplay> langkah = new ArrayList<>();
            for (JsonObject l : m.getObjectList("langkah")) {
                List<String> barisLog = new ArrayList<>();
                Object rawLog = JsonParser.parse(l.getString("logJson", "[]"));
                if (rawLog instanceof List) {
                    for (Object item : (List<Object>) rawLog) {
                        if (item != null) barisLog.add(item.toString());
                    }
                }
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
                        barisLog));
            }
            hasil.add(new RekamanPertandingan(
                    m.getInt("id", hasil.size() + 1),
                    m.getString("waktu"),
                    m.getString("trainer1"),
                    m.getString("trainer2"),
                    m.getString("pemenang"),
                    m.getBoolean("menyerah", false),
                    m.getInt("totalRonde"),
                    m.getString("tim1", ""),
                    m.getString("tim2", ""),
                    langkah));
        }
        return hasil;
    }

    private void tulisLokal(List<RekamanPertandingan> daftar) {
        StringBuilder sb = new StringBuilder("{\n  \"pertandingan\": [\n");
        for (int i = 0; i < daftar.size(); i++) {
            if (i > 0) sb.append(",\n");
            RekamanPertandingan r = daftar.get(i);
            sb.append("    {\n");
            sb.append("      \"id\": ").append(r.getId()).append(",\n");
            sb.append("      \"waktu\": ").append(JsonWriter.str(r.getWaktu())).append(",\n");
            sb.append("      \"trainer1\": ").append(JsonWriter.str(r.getTrainer1())).append(",\n");
            sb.append("      \"trainer2\": ").append(JsonWriter.str(r.getTrainer2())).append(",\n");
            sb.append("      \"pemenang\": ").append(JsonWriter.str(r.getPemenang())).append(",\n");
            sb.append("      \"menyerah\": ").append(r.isMenyerah()).append(",\n");
            sb.append("      \"totalRonde\": ").append(r.getTotalRonde()).append(",\n");
            sb.append("      \"tim1\": ").append(JsonWriter.str(r.getRingkasanTim1())).append(",\n");
            sb.append("      \"tim2\": ").append(JsonWriter.str(r.getRingkasanTim2())).append(",\n");
            sb.append("      \"langkah\": [\n");
            List<LangkahReplay> ls = r.getLangkahList();
            for (int j = 0; j < ls.size(); j++) {
                if (j > 0) sb.append(",\n");
                LangkahReplay l = ls.get(j);
                sb.append("        { ")
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
            sb.append("\n      ]\n    }");
        }
        sb.append("\n  ]\n}\n");
        try {
            if (fileLokal.getParent() != null) Files.createDirectories(fileLokal.getParent());
            Files.writeString(fileLokal, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
