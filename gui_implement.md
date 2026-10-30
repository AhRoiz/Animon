**Role:**
Bertindaklah sebagai Senior Java Desktop Developer (Swing) dan UI/UX Game Integrator. Tugasmu adalah membuat Antarmuka Grafis (GUI) untuk game *turn-based battle* "Animon: Elemental Clash" yang *core logic*-nya (mesin OOP, database, CLI) sudah selesai dibuat.

**Konteks & Tujuan:**
- Mengganti antarmuka CLI/Terminal yang sudah ada dengan GUI berbasis Java Swing.
- Dilarang merombak/mengubah *business logic* inti yang sudah ada di `GameManager`, `BattleEngine`, atau hierarki `Hewan`/`Item`. Tugasmu hanya membuat *View* dan menghubungkannya dengan *Controller* (`GameManager`).
- Tema Visual: Retro Game Boy. Gunakan palet 4 warna hijau (misal: `#0f380f` (Gelap), `#306230`, `#8bac0f`, `#9bbc0f` (Terang)) dan *custom font* bergaya pixel/retro (jika font tidak ada, gunakan font Monospaced tebal).

**Manajemen Aset (Penting!):**
- Sprite hewan berukuran 64x64 piksel sudah tersedia di direktori `src/animon/assets/`.
- Format penamaan file sudah standar: `[namaHewanCamelCase]_depan.png` (untuk musuh di atas) dan `[namaHewanCamelCase]_belakang.png` (untuk pemain aktif di bawah).
- Saat me-load *image* ke `JLabel`, gunakan `Image.SCALE_REPLICATE` atau teknik *nearest-neighbor interpolation* saat me-resize gambar agar hasil *scaling* tetap *pixelated* dan tajam, tidak *blur*.

**Arsitektur GUI (Gunakan CardLayout):**
Buat sebuah JFrame utama (`MainFrame` atau integrasikan ke `GameFrame`) yang menggunakan `CardLayout` untuk berpindah-pindah layar (Panel) berikut ini dengan mulus tanpa membuka window baru berulang kali (kecuali untuk HistoryFrame):

**1. MainMenuPanel (Sesuai Wireframe L.1)**
- Judul Game "ANIMON: ELEMENTAL CLASH".
- Tombol: NEW GAME, LOAD GAME, RIWAYAT PERTANDINGAN, EXIT.

**2. SetupPanel / Persiapan Trainer (Sesuai Wireframe L.2)**
- Input `JTextField` untuk nama Trainer 1 dan Trainer 2.
- Menampilkan teks daftar 6 Animon acak/pilihan untuk masing-masing trainer.
- Tombol "MULAI DRAFT ITEM".

**3. DraftPanel / Layar Pilihan Item (Sesuai Wireframe L.5)**
- Panel ini dapat di- *reuse* untuk Draft Awal dan Kill Reward.
- Menampilkan 4 opsi item (menggunakan `JButton` atau `JList`).
- Menampilkan status Inventory aktif di sebelah kanan.

**4. BlindTransitionPanel (Sesuai Wireframe L.3)**
- Panel menutupi seluruh layar (warna background hijau gelap solid).
- Label teks: "GILIRAN PLAYER X SELESAI. Serahkan perangkat...".
- Tombol besar: "SIAP!" untuk lanjut ke layar Battle.

**5. GameFrame / BattlePanel (Sesuai Wireframe L.4 - Layar Utama)**
- Terbagi menjadi 3 area utama:
  - **Arena Atas:** Status Musuh (Nama, Level, Tipe/Elemen), `JProgressBar` (HP), dan `JLabel` untuk Sprite Depan musuh.
  - **Arena Bawah:** Status Player Aktif (Nama, Level, Tipe/Elemen), `JProgressBar` (HP & Mana), dan `JLabel` untuk Sprite Belakang player aktif.
  - **Battle Box (Bawah):** 
    - Sebelah Kiri: `JTextArea` untuk Dialog/Log pertempuran (misal: "Banteng Gunung memakai Seruduk Longsor!").
    - Sebelah Kanan: 4 Tombol Aksi (FIGHT, SWITCH, ITEM, DEFEND). 
    - Saat FIGHT diklik, ubah panel tombol menjadi pilihan serangan (Biasa / Skill).

**6. ShopPanel (Sesuai Wireframe L.6)**
- Menampilkan Gold pemain.
- Dua `JList` atau tabel: Daftar Beli (Stok Shop) dan Daftar Jual (Inventory).
- Tombol BELI, JUAL, KELUAR.

**7. HistoryFrame / Replay (Sesuai Wireframe L.7)**
- Bisa berupa Window baru (`JFrame`) terpisah.
- Kiri: Tabel/List daftar match dari MySQL.
- Kanan (Area Replay): 
  - `JTextArea` untuk log langkah spesifik.
  - `JProgressBar` animasi statis untuk melihat perubahan HP Pelaku dan Target di langkah tersebut.
  - Tombol kontrol: `< PREV`, `NEXT >`, `AUTO PLAY` (Gunakan `javax.swing.Timer`).

**Instruksi Implementasi untuk Agent:**
1. Buat terlebih dahulu kerangka kelas GUI (Panel-panel) menggunakan palet warna Retro Game Boy.
2. Buat mekanisme pemuatan gambar sprite dari path `src/animon/assets/` secara dinamis dengan mencocokkan string nama objek `Hewan` yang sedang aktif.
3. Hubungkan *Event Listeners* (`ActionListener`) dari setiap tombol GUI agar memanggil method yang sudah ada di `GameManager` (contoh: `btnFight.addActionListener(e -> gameManager.prosesAksi("FIGHT"));`).
4. Pastikan update UI (seperti perubahan nilai JProgressBar) di-*trigger* setelah `BattleEngine` selesai menghitung *damage*.