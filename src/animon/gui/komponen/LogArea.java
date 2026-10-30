package animon.gui.komponen;

import animon.gui.tema.Tema;

import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * {@link JTextArea} untuk dialog/log pertempuran dengan efek mesin ketik (teks baru muncul
 * huruf demi huruf, seperti kotak dialog game Game Boy). Klik area log untuk melewati animasi.
 */
public class LogArea extends JTextArea {

    private String target = "";
    private final Timer ketik;

    public LogArea() {
        setEditable(false);
        setLineWrap(true);
        setWrapStyleWord(true);
        setFont(Tema.teks(22f));
        setForeground(Tema.GELAP);
        setBackground(Tema.TERANG);
        setCaretColor(Tema.TERANG);
        setFocusable(false);
        setMargin(new java.awt.Insets(4, 8, 4, 8));

        ketik = new Timer(12, e -> langkah());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lewati();
            }
        });
    }

    /** Ganti seluruh isi log. Bagian yang sama dengan isi lama tidak diketik ulang. */
    public void setBaris(List<String> baris) {
        StringBuilder sb = new StringBuilder();
        for (String b : baris) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(format(b));
        }
        String baru = sb.toString();
        String lama = getText();
        target = baru;
        if (!baru.startsWith(lama)) {
            // log diganti total (giliran baru) -> ketik ulang dari awal agar pemain membaca aksi lawan
            setText("");
        }
        if (!ketik.isRunning()) ketik.start();
    }

    /** Teks langsung, tanpa animasi. */
    public void setLangsung(String teks) {
        ketik.stop();
        target = teks;
        setText(teks);
        setCaretPosition(0);
    }

    public void lewati() {
        if (!target.equals(getText())) {
            ketik.stop();
            setText(target);
            setCaretPosition(getDocument().getLength());
        }
    }

    public boolean isMengetik() {
        return ketik.isRunning();
    }

    private void langkah() {
        String cur = getText();
        if (cur.length() >= target.length() || !target.startsWith(cur)) {
            if (!target.equals(cur)) setText(target);
            ketik.stop();
            setCaretPosition(getDocument().getLength());
            return;
        }
        int n = Math.min(target.length(), cur.length() + 3);
        append(target.substring(cur.length(), n));
        setCaretPosition(getDocument().getLength());
    }

    /** Penanda visual sederhana untuk baris penting (JTextArea tidak mendukung warna). */
    private static String format(String b) {
        if (b.startsWith("---")) return b.replace("---", "==");
        if (b.startsWith("WEAK!") || b.startsWith("CRITICAL") || b.startsWith("ONE MORE")) return "!! " + b;
        if (b.endsWith("pingsan!") || b.endsWith("tumbang!")) return "xx " + b;
        return "> " + b;
    }
}
