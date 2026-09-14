# BRIPORT — Android Robot + PWA + Apps Script

BRIPORT adalah platform internal untuk membantu PPBK menyiapkan dan mengirim Pelaporan Aktivitas BRILink melalui Google Forms resmi.

## Arsitektur
1. PWA BRIPORT menerima Unit Kerja, PPBK, Jenis Pelaporan, ID Outlet, Nama Agen, dan foto bukti.
2. Apps Script membuat job dan URL prefilled dari Form resmi.
3. Android BRIPORT menerima `briport://run?jobId=...`.
4. BRIPORT mengambil bukti dari backend, menyimpannya ke `Download/BRIPORT`, lalu membuka Chrome.
5. AccessibilityService membantu klik navigasi, membuka pemilih file, memilih bukti, dan mengirim form setelah bukti terdeteksi.

## Konfigurasi yang sudah dikunci
- Form ID: `1mSgM-Smettzn8UwVRG3Q3IF-KXTwhzMhtCmwBpPrYfg`
- BO: Cianjur
- Role: PPBK
- 27 Unit Kerja Cianjur
- 6 nama PPBK
- 13 jenis pelaporan

## Build
- JDK 17
- Gradle 8.9
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21
- compileSdk 35 / targetSdk 35 / minSdk 29

GitHub Actions berada di `.github/workflows/build-apk.yml`.

## Penting
AccessibilityService harus diaktifkan manual oleh pengguna dari Settings Android. BRIPORT tidak mengubah atau melewati kontrol keamanan Android. Penggunaan Accessibility API harus memiliki disclosure dan consent yang jelas bila aplikasi didistribusikan melalui Google Play.

## Pengembangan ke depan
Arsitektur dipisah menjadi katalog laporan, Android robot, PWA, dan backend job. Karena itu fitur seperti dashboard, riwayat laporan, approval, multi-BO, role PEBM/Kordinator/RMFT, notifikasi, dan sinkronisasi dapat ditambahkan tanpa mengganti fondasi aplikasi.
