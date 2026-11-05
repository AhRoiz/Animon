package animon.gui.riwayat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Rekaman utuh satu pertandingan yang sudah selesai beserta daftar langkah replay-nya. */
public final class RekamanPertandingan {

    private final long id;
    private final String waktu;
    private final String trainer1;
    private final String trainer2;
    private final String pemenang;
    private final boolean menyerah;
    private final int totalRonde;
    private final String ringkasanTim1;
    private final String ringkasanTim2;
    private final List<LangkahReplay> langkahList;

    public RekamanPertandingan(long id, String waktu, String trainer1, String trainer2,
                               String pemenang, boolean menyerah, int totalRonde,
                               String ringkasanTim1, String ringkasanTim2,
                               List<LangkahReplay> langkahList) {
        this.id = id;
        this.waktu = Objects.requireNonNull(waktu);
        this.trainer1 = Objects.requireNonNull(trainer1);
        this.trainer2 = Objects.requireNonNull(trainer2);
        this.pemenang = Objects.requireNonNull(pemenang);
        this.menyerah = menyerah;
        this.totalRonde = totalRonde;
        this.ringkasanTim1 = ringkasanTim1 == null ? "" : ringkasanTim1;
        this.ringkasanTim2 = ringkasanTim2 == null ? "" : ringkasanTim2;
        this.langkahList = Collections.unmodifiableList(new ArrayList<>(langkahList));
    }

    public long getId()                       { return id; }
    public String getWaktu()                  { return waktu; }
    public String getTrainer1()               { return trainer1; }
    public String getTrainer2()               { return trainer2; }
    public String getPemenang()               { return pemenang; }
    public boolean isMenyerah()               { return menyerah; }
    public int getTotalRonde()                { return totalRonde; }
    public String getRingkasanTim1()          { return ringkasanTim1; }
    public String getRingkasanTim2()          { return ringkasanTim2; }
    public List<LangkahReplay> getLangkahList(){ return langkahList; }
}

