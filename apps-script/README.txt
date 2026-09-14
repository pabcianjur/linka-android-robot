BRIPORT FINAL BACKEND

1. Buka Apps Script project backend yang sudah dipakai BRIPORT.
2. Ganti seluruh Code.gs dengan Code.gs di folder ini.
3. Simpan.
4. Deploy > Manage deployments > Web app > New version.
5. Execute as: Me.
6. Who has access: Anyone.
7. Gunakan URL /exec yang sama jika deployment diedit.

Backend tidak melakukan submit GForm secara programatik. Backend hanya menyimpan foto, membuat JOB, dan menghasilkan prefilled URL melalui FormApp. Android Robot yang membuka Chrome, memilih file bukti, lalu mengirim GForm resmi.
