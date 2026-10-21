package animon.battle;

import animon.model.Buff;
import animon.model.Hewan;

import java.util.Objects;

/**
 * Efek (buff) yang menunggu di antrian {@link BattleEngine}.
 * {@code sisaDelay} dikurangi di awal setiap giliran pemilik target; saat mencapai 0
 * buff diterapkan ke hewan target.
 */
public final class EfekTertunda {

    private final Hewan target;
    private final Buff buff;
    private int sisaDelay;

    public EfekTertunda(Hewan target, Buff buff, int delayTurn) {
        this.target = Objects.requireNonNull(target, "target");
        this.buff = Objects.requireNonNull(buff, "buff");
        if (delayTurn < 1) throw new IllegalArgumentException("delayTurn efek tertunda minimal 1");
        this.sisaDelay = delayTurn;
    }

    void kurangiDelay() {
        sisaDelay--;
    }

    public boolean isSiap() {
        return sisaDelay <= 0;
    }

    public Hewan getTarget() { return target; }
    public Buff getBuff()    { return buff; }
    public int getSisaDelay(){ return sisaDelay; }
}
