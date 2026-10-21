package animon.battle;

import animon.enums.StatType;
import animon.enums.Tipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabel Type Advantage (tipe fisik penyerang vs tipe fisik bertahan).
 *
 * <pre>
 *  Darat   -&gt; Terbang : sulit menjangkau udara (miss +35%)
 *  Darat   -&gt; Robot   : baja meredam (dmg x0.75)
 *  Terbang -&gt; Darat   : menyambar dari atas (crit +15%)
 *  Terbang -&gt; Robot   : paruh vs baja (dmg x0.85)
 *  Air     -&gt; Darat   : tanah berlumpur (50% debuff SPD-30% 2 giliran)
 *  Air     -&gt; Robot   : korsleting (crit +25%, dmg x1.2)
 *  Air     -&gt; Terbang : sulit mencapai langit (miss +15%)
 *  Naga    -&gt; Naga    : darah naga beradu (crit +20%, dmg x1.25)
 *  Naga    -&gt; Robot   : baja menahan (dmg x0.8)
 *  Naga    -&gt; lainnya : aura naga (dmg x1.1)
 *  Robot   -&gt; Terbang : sistem pembidik (miss -10%)
 *  Robot   -&gt; Air     : sirkuit basah (dmg x0.85)
 * </pre>
 */
public final class TypeChart {

    private TypeChart() {
    }

    public static EfekTipe cek(Tipe penyerang, Tipe bertahan) {
        switch (penyerang) {
            case DARAT:
                if (bertahan == Tipe.TERBANG) return new EfekTipe(35, 0, 1.0, "Sulit menjangkau lawan di udara");
                if (bertahan == Tipe.ROBOT) return new EfekTipe(0, 0, 0.75, "Lapisan baja meredam serangan darat");
                break;
            case TERBANG:
                if (bertahan == Tipe.DARAT) return new EfekTipe(0, 15, 1.0, "Menyambar dari atas");
                if (bertahan == Tipe.ROBOT) return new EfekTipe(0, 0, 0.85, "Paruh beradu dengan baja");
                break;
            case AIR:
                if (bertahan == Tipe.DARAT) {
                    return new EfekTipe(0, 0, 1.0, "Arus air membuat tanah berlumpur")
                            .denganDebuff("Terjebak Lumpur", StatType.SPEED, -30, 2, 50);
                }
                if (bertahan == Tipe.ROBOT) return new EfekTipe(0, 25, 1.2, "Korsleting!");
                if (bertahan == Tipe.TERBANG) return new EfekTipe(15, 0, 1.0, "Semburan air sulit mencapai langit");
                break;
            case NAGA:
                if (bertahan == Tipe.NAGA) return new EfekTipe(0, 20, 1.25, "Darah naga beradu");
                if (bertahan == Tipe.ROBOT) return new EfekTipe(0, 0, 0.8, "Baja menahan cakar naga");
                return new EfekTipe(0, 0, 1.1, "Aura naga menekan lawan");
            case ROBOT:
                if (bertahan == Tipe.TERBANG) return new EfekTipe(-10, 0, 1.0, "Sistem pembidik mengunci target");
                if (bertahan == Tipe.AIR) return new EfekTipe(0, 0, 0.85, "Sirkuit basah, tenaga berkurang");
                break;
            default:
                break;
        }
        return EfekTipe.NETRAL;
    }

    /** Daftar semua interaksi non-netral (untuk layar INFO). */
    public static List<String> ringkasan() {
        List<String> hasil = new ArrayList<>();
        for (Tipe a : Tipe.values()) {
            for (Tipe b : Tipe.values()) {
                EfekTipe e = cek(a, b);
                if (!e.isNetral()) {
                    hasil.add(String.format("%-7s -> %-7s : %s", a.getLabel(), b.getLabel(), e.ringkasanAngka()));
                }
            }
        }
        return hasil;
    }
}
