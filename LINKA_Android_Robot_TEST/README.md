# LINKA Robot TEST — Android Accessibility Stage 1

Tujuan tahap ini:
- membuka Google Form resmi dengan data prefilled;
- menguji AccessibilityService pada Chrome;
- otomatis menekan tombol "Berikutnya" pada halaman Google Forms jika ditemukan;
- BERHENTI sebelum upload/Kirim;
- tidak membuat submission baru.

## Data TEST bawaan
- Branch Office: Cianjur
- Unit Kerja: 4063 - Bojongpicung
- Role: PPBK
- Nama PPBK: Abdul Azzis Muslim
- Jenis: Reaktivasi Agen
- ID Outlet: 11223344
- Nama Agen: Kirana cell

## Cara menjalankan
1. Buka project ini di Android Studio.
2. Tunggu Gradle Sync selesai.
3. Run ke HP Android.
4. Buka aplikasi LINKA Robot TEST.
5. Tekan "1. AKTIFKAN ROBOT".
6. Di Accessibility/Installed apps, aktifkan "LINKA Robot GForm".
7. Kembali ke aplikasi.
8. Tekan "2. MULAI ROBOT TEST".
9. Chrome akan membuka GForm prefilled.
10. Robot mencoba maju antar halaman dengan tombol "Berikutnya".
11. Robot sengaja berhenti sebelum bagian upload/Kirim.

## Catatan
AccessibilityService di Android harus diaktifkan oleh pengguna melalui Settings. Tahap ini sengaja dibuat sebagai TEST dan safety-lock agar tidak menekan "Kirim".

Google Forms dapat berubah tampilan/struktur, sehingga pencocokan teks pada tahap ini adalah baseline untuk pengujian di HP pengguna.
