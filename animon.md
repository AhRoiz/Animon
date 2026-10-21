**Role:**
Bertindaklah sebagai Senior Java Developer dan Game Architect. Tugasmu adalah membuat kerangka kode dan implementasi game *turn-based battle* (mirip Pokemon/Animal Kaiser/Persona) berbasis teks/CLI di Java. Kode harus ditulis dengan standar Object-Oriented Programming (OOP) yang ketat, bersih, terstruktur, dan diberi komentar penjelasan.

**Konteks Game:**
- Tipe Game: 1v1 Local Multiplayer (Hotseat). Pemain bergantian menggunakan satu device.
- Tema: Pertarungan antar Trainer, masing-masing membawa maksimal 6 Hewan. Siapa yang kehabisan hewan duluan akan kalah.
- UI: Berbasis Teks/CLI, namun memiliki menu interaktif yang rapi (FIGHT, SWITCH, ITEM, DEFEND).
- Wajib mendemonstrasikan pilar OOP: Inheritance, Polymorphism, Encapsulation, dan Abstraction (Wajib ada Abstract Class dan Interface).

**Struktur Arsitektur & Aturan OOP yang Wajib DiimplemeFntasikan:**

**1. Data, File I/O, dan Database 10 Hewan (Sangat Penting)**
- Dilarang keras melakukan *hardcode* pembuatan objek Hewan di Main class.
- Buat class `GameSeeder` atau `CardFactory` yang membaca data hewan dari file eksternal (WAJIB format JSON).
- **TUGAS TAMBAHAN:** Buatkan juga *output* berupa struktur file `data_hewan.json` yang berisi **minimal 10 hewan berbeda**. 
- Kesepuluh hewan ini harus memiliki variasi yang kaya:
  - Ada yang dari kelas Darat, Udara/Terbang, dan Air.
  - Kombinasikan dengan Role (DPS, Support, Tank) dan Elemen (Api, Air, Earth, Ice, Normal).
  - Stat mereka harus merepresentasikan kelebihan dan kekurangan tipe tersebut (Misal: Hewan Terbang punya evasion/speed tinggi tapi HP rendah; Hewan Darat/Tank punya HP sangat tebal tapi rawan terhadap elemen tertentu).

**2. Class Trainer**
- **Atribut:** `String nama`, `List<Hewan> tim` (maks 6), `Item[] inventory` (maks 6), `int gold`.
- **Method:** `SwitchHewan(int index)`, `TambahItem(Item item)` (dengan validasi batas inventory 6, paksa jual/buang jika penuh), `GunakanItem()`.

**3. Hierarki Class Hewan (Karakter Utama)**
- Buat **Abstract Class** `Hewan`.
  - **Atribut:** `nama`, `hp`, `maxHp`, `mana`, `maxMana`, `level`, `boolean isDefending`.
  - **Enum Atribut:** `Tipe` (Darat, Terbang, Air, Naga, Robot) dan `Elemen` (Api, Air, Earth, Ice, Normal).
  - **Abstract Method:** `GunakanSkill(Hewan target)` untuk mengimplementasikan *Polymorphism*.
- Buat **Child Class** berdasarkan Role:
  - `HewanDPS`: Fokus pada damage tinggi.
  - `HewanSupport`: Skill bisa melakukan *Lifesteal* (damage musuh dan nambah HP sendiri) atau heal kawan.
  - `HewanTank`: Punya HP tinggi atau skill bertahan murni (Taunt/Shield).

**4. Hierarki Class Item (Inventory & Ekonomi)**
- Buat **Abstract Class** `Item` (atau Interface `IUsable`).
  - **Atribut:** `nama`, `hargaBeli`, `hargaJual` (otomatis diset 50% dari harga beli).
  - **Abstract Method:** `Gunakan(Hewan target)`.
- Buat **Child Class**:
  - `ItemHeal`: Menambah HP/Mana.
  - `ItemBuff`: Memberikan status buff sementara.
  - `ItemEvolusi`: Mengubah tipe/elemen hewan target (misal Normal menjadi Api) atau menaikkan max *stats*.

**5. Game Engine & Sistem Battle**
- **Sistem Transisi (Hotseat):** Buat layar transisi antar giliran ("Giliran Player 1 selesai. Tekan Enter untuk lanjut ke Player 2") yang membersihkan layar console (`clear screen`) agar musuh tidak bisa menyontek.
- **BattleEngine (Damage Calculator):** 
  - Kalkulasi *Type Advantage* (Miss/Crit/Resist): Misal `Darat` menyerang `Terbang` memiliki peluang besar untuk *miss*. `Air` melawan `Darat` bisa memberikan efek tertentu.
  - Kalkulasi *Elemental Weakness* (Ala Persona): Jika elemen serangan super efektif (misal Air vs Api), berikan *damage multiplier* (x2) atau status "One More".
  - Kalkulasi Defend: Jika `isDefending == true`, kurangi *damage* yang diterima.
- **Sistem Delayed Buff:** Implementasikan sebuah *Queue* atau *List* di BattleEngine untuk mengantre efek buff/skill. Buff dengan `delayTurn = 1` baru akan aktif teraplikasi ke hewan pada giliran berikutnya.

**6. Flow Permainan & Mekanik Tambahan**
- **Drafting Item (Gacha):** Buat fungsi `TampilkanOpsiItem()`. Di awal game, tampilkan 4 item acak, pemain memilih 2.
- **Kill Reward:** Setiap kali satu hewan musuh mati (`hp <= 0` / `IsFainted()`), panggil fungsi `TampilkanOpsiItem()` untuk menampilkan 4 opsi item, pemain memilih 1.
- **Shop System:** Buat class `Shop`. Di titik tertentu (mid-game atau antar fase), Trainer bisa mengakses Shop untuk `Beli()` item dengan *gold*, atau `Jual()` item dari inventory jika penuh.

**Instruksi Output:**
1. Tolong buatkan isi konten untuk file `data_hewan.json` (minimal 10 hewan) terlebih dahulu agar strukturnya jelas.
2. Buatkan implementasi Java-nya secara modular. Mulai dari Enum, Abstract Class, dan Interface, lalu lanjutkan ke Child Class. 
3. Buat logic untuk BattleEngine (termasuk kalkulasi kelemahan tipe/elemen) dan sistem drafting/shop-nya.
4. Jangan lupa berikan contoh fungsi *parsing* JSON di Java (bisa diasumsikan menggunakan library Gson/Jackson atau *parser* manual sederhana).