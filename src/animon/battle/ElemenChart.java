package animon.battle;

import animon.enums.Efektivitas;
import animon.enums.Elemen;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tabel kelemahan elemen (ala Persona).
 * Siklus: AIR &gt; API &gt; ICE &gt; EARTH &gt; AIR.
 * <ul>
 *   <li>Penyerang mengalahkan bertahan  -&gt; LEMAH (x2, memicu ONE MORE)</li>
 *   <li>Bertahan mengalahkan penyerang / elemen sama -&gt; RESIST (x0.5)</li>
 *   <li>NORMAL selalu netral</li>
 * </ul>
 */
public final class ElemenChart {

    public static final double MULT_LEMAH = 2.0;
    public static final double MULT_RESIST = 0.5;

    private static final Map<Elemen, Elemen> MENGALAHKAN = new EnumMap<>(Elemen.class);

    static {
        MENGALAHKAN.put(Elemen.AIR, Elemen.API);
        MENGALAHKAN.put(Elemen.API, Elemen.ICE);
        MENGALAHKAN.put(Elemen.ICE, Elemen.EARTH);
        MENGALAHKAN.put(Elemen.EARTH, Elemen.AIR);
    }

    private ElemenChart() {
    }

    public static Efektivitas cek(Elemen serangan, Elemen bertahan) {
        if (serangan == Elemen.NORMAL || bertahan == Elemen.NORMAL) return Efektivitas.NETRAL;
        if (MENGALAHKAN.get(serangan) == bertahan) return Efektivitas.LEMAH;
        if (MENGALAHKAN.get(bertahan) == serangan || serangan == bertahan) return Efektivitas.RESIST;
        return Efektivitas.NETRAL;
    }

    public static double multiplier(Efektivitas e) {
        switch (e) {
            case LEMAH:  return MULT_LEMAH;
            case RESIST: return MULT_RESIST;
            default:     return 1.0;
        }
    }

    public static String ringkasan() {
        return "Air > Api > Ice > Earth > Air";
    }
}
