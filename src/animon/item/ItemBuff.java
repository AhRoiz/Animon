package animon.item;

import animon.battle.EfekTertunda;
import animon.battle.HasilAksi;
import animon.enums.JenisItem;
import animon.enums.StatType;
import animon.model.Buff;
import animon.model.Hewan;

import java.util.Objects;

/**
 * Child class item buff sementara.
 *
 * Bila {@code delayTurn == 0} buff langsung menempel ke hewan. Bila {@code delayTurn > 0}
 * item TIDAK menerapkan buff sendiri, melainkan menitipkan {@link EfekTertunda} di
 * {@link HasilAksi}; BattleEngine lalu memasukkannya ke antrian dan baru menerapkannya
 * pada giliran berikutnya (Sistem Delayed Buff).
 */
public class ItemBuff extends Item {

    private final StatType stat;
    private final int nilai;
    private final int durasi;
    private final int delayTurn;

    public ItemBuff(String id, String nama, int hargaBeli, String deskripsi,
                    StatType stat, int nilai, int durasi, int delayTurn) {
        super(id, nama, hargaBeli, deskripsi);
        this.stat = Objects.requireNonNull(stat, "stat buff");
        if (stat == StatType.TAUNT) throw new IllegalArgumentException("Item tidak boleh memberi TAUNT");
        if (nilai == 0) throw new IllegalArgumentException("nilai buff tidak boleh 0");
        if (durasi < 1) throw new IllegalArgumentException("durasi buff minimal 1");
        if (delayTurn < 0) throw new IllegalArgumentException("delayTurn tidak boleh negatif");
        this.nilai = nilai;
        this.durasi = durasi;
        this.delayTurn = delayTurn;
    }

    @Override
    public JenisItem getJenis() {
        return JenisItem.BUFF;
    }

    @Override
    public String cekPenggunaan(Hewan target) {
        return cekTargetHidup(target);
    }

    /** Buff baru setiap pemakaian (Buff punya state durasi, jadi tidak boleh dipakai bersama). */
    private Buff buatBuff() {
        return new Buff(getNama(), stat, nilai, durasi);
    }

    @Override
    public HasilAksi gunakan(Hewan target) {
        String alasan = cekPenggunaan(target);
        if (alasan != null) return HasilAksi.gagal(alasan);

        HasilAksi hasil = awalLog(target);
        Buff buff = buatBuff();
        if (delayTurn > 0) {
            hasil.tunda(new EfekTertunda(target, buff, delayTurn));
            return hasil.log("Efek " + buff.getLabel() + " akan aktif pada " + target.getNama()
                    + " dalam " + delayTurn + " giliran lagi.");
        }
        target.tambahBuff(buff);
        return hasil.log(target.getNama() + " mendapat buff " + buff.getLabel() + ".");
    }

    @Override
    public String getRingkasan() {
        String satuan = (stat == StatType.EVASION || stat == StatType.CRIT) ? "% poin" : "%";
        String s = stat.getLabel() + (nilai > 0 ? "+" : "") + nilai + satuan + " selama " + durasi + " giliran";
        if (delayTurn > 0) s += " (aktif +" + delayTurn + " giliran)";
        return s;
    }

    public StatType getStat()  { return stat; }
    public int getNilai()      { return nilai; }
    public int getDurasi()     { return durasi; }
    public int getDelayTurn()  { return delayTurn; }
}
