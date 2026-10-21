package animon.battle;

import animon.enums.Efektivitas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Detail hasil kalkulasi satu serangan oleh {@link DamageCalculator}. */
public final class HasilSerangan {

    private final List<String> log = new ArrayList<>();
    private boolean kena;
    private boolean crit;
    private Efektivitas efektivitas = Efektivitas.NETRAL;
    private int damage;
    private int diserapShield;
    private int hpHilang;
    private boolean targetPingsan;
    private boolean oneMore;

    HasilSerangan() {
    }

    void log(String baris)               { log.add(baris); }
    void setKena(boolean kena)           { this.kena = kena; }
    void setCrit(boolean crit)           { this.crit = crit; }
    void setEfektivitas(Efektivitas e)   { this.efektivitas = e; }
    void setDamage(int damage)           { this.damage = damage; }
    void setDiserapShield(int n)         { this.diserapShield = n; }
    void setHpHilang(int n)              { this.hpHilang = n; }
    void setTargetPingsan(boolean b)     { this.targetPingsan = b; }
    void setOneMore(boolean b)           { this.oneMore = b; }

    public boolean isKena()              { return kena; }
    public boolean isCrit()              { return crit; }
    public Efektivitas getEfektivitas()  { return efektivitas; }
    public int getDamage()               { return damage; }
    public int getDiserapShield()        { return diserapShield; }
    public int getHpHilang()             { return hpHilang; }
    public boolean isTargetPingsan()     { return targetPingsan; }
    public boolean isOneMore()           { return oneMore; }
    public List<String> getLog()         { return Collections.unmodifiableList(log); }

    /** Damage yang benar-benar "masuk" (HP + shield), dipakai Lifesteal. */
    public int getDamageMasuk() {
        return hpHilang + diserapShield;
    }
}
