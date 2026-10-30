package animon.gui.komponen;

import animon.gui.tema.Tema;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * {@link JProgressBar} bergaya bar HP/MP Game Boy (tersegmentasi, bingkai pixel).
 *
 * <p>{@link #setNilaiAnimasi(int, int)} menggerakkan bar secara halus dari nilai lama ke nilai baru
 * (dipanggil setelah {@code BattleEngine} selesai menghitung damage), sehingga perubahan HP
 * terlihat "turun perlahan" seperti di game aslinya. Bar HP berkedip saat sisa &lt; 25%.</p>
 */
public class PixelBar extends JProgressBar {

    public enum Jenis { HP, MP }

    private final Jenis jenis;
    private final Timer tween;
    private int target;
    private boolean kedipTampil = true;
    private final Timer kedip;
    private Runnable saatSelesai;

    public PixelBar(Jenis jenis) {
        super(0, 100);
        this.jenis = jenis;
        setUI(new UI());
        setBorderPainted(false);
        setOpaque(false);
        setStringPainted(false);

        tween = new Timer(16, e -> langkah());
        kedip = new Timer(380, e -> {
            kedipTampil = !kedipTampil;
            repaint();
        });
    }

    /** Set langsung tanpa animasi. */
    public void setNilai(int nilai, int maks) {
        tween.stop();
        setMaximum(Math.max(1, maks));
        target = Math.max(0, Math.min(nilai, getMaximum()));
        setValue(target);
        perbaruiKedip();
    }

    /** Animasi halus dari nilai sekarang menuju {@code nilai}. */
    public void setNilaiAnimasi(int nilai, int maks) {
        setNilaiAnimasi(nilai, maks, null);
    }

    public void setNilaiAnimasi(int nilai, int maks, Runnable selesai) {
        if (maks != getMaximum()) setMaximum(Math.max(1, maks));
        target = Math.max(0, Math.min(nilai, getMaximum()));
        saatSelesai = selesai;
        if (getValue() == target) {
            perbaruiKedip();
            if (selesai != null) selesai.run();
            return;
        }
        tween.start();
    }

    private void langkah() {
        int v = getValue();
        int selisih = target - v;
        int step = Math.max(1, Math.abs(selisih) / 8);
        step = Math.min(step, Math.max(1, getMaximum() / 40));
        if (Math.abs(selisih) <= step) {
            setValue(target);
            tween.stop();
            perbaruiKedip();
            if (saatSelesai != null) {
                Runnable r = saatSelesai;
                saatSelesai = null;
                r.run();
            }
        } else {
            setValue(v + Integer.signum(selisih) * step);
        }
    }

    private void perbaruiKedip() {
        boolean kritis = jenis == Jenis.HP && getValue() > 0 && getValue() * 4 < getMaximum();
        if (kritis && !kedip.isRunning()) kedip.start();
        if (!kritis) {
            kedip.stop();
            kedipTampil = true;
        }
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, jenis == Jenis.HP ? 16 : 12);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        tween.stop();
        kedip.stop();
    }

    /** UI pixel: latar terang, isi segmen gelap, label kecil "HP"/"MP" ditangani di luar komponen. */
    private final class UI extends BasicProgressBarUI {
        @Override
        public void paint(Graphics g0, JComponent c) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.pixelated(g);
            int w = c.getWidth();
            int h = c.getHeight();

            // bingkai gelap dengan sudut terpotong
            g.setColor(Tema.GELAP);
            g.fillRect(2, 0, w - 4, h);
            g.fillRect(0, 2, w, h - 4);
            g.setColor(Tema.TERANG);
            g.fillRect(3, 3, w - 6, h - 6);

            int dalamW = w - 8;
            double rasio = getMaximum() == 0 ? 0 : (double) getValue() / getMaximum();
            int isi = (int) Math.round(dalamW * rasio);
            if (getValue() > 0 && isi == 0) isi = 2;

            Color warna = jenis == Jenis.HP ? Tema.GELAP : Tema.TUA;
            if (jenis == Jenis.HP && !kedipTampil) warna = Tema.MUDA;
            g.setColor(warna);
            g.fillRect(4, 4, isi, h - 8);
            // highlight atas
            g.setColor(jenis == Jenis.HP ? Tema.TUA : Tema.MUDA);
            g.fillRect(4, 4, isi, Math.max(1, (h - 8) / 4));

            // segmen setiap 6px agar terlihat seperti bar LCD
            g.setColor(Tema.TERANG);
            for (int x = 4 + 6; x < 4 + isi; x += 6) g.fillRect(x, 4, 1, h - 8);
            g.dispose();
        }
    }
}
