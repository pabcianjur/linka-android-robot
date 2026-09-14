package com.briport.app

object ReportCatalog {
    val units = arrayOf(
        "4070 - Kadupandak","4073 - Saganten","4076 - Tanggeung","4086 - Pagelaran","7471 - Agrabinta","7732 - Cibinong",
        "4063 - Bojongpicung","4067 - Cipetir","4069 - Ciranjang Baru","4079 - Ciranjang","4080 - Pamoyanan","4081 - Sawahgede",
        "3453 - Cipanas","4064 - Ciherang","4065 - Cijedil","4066 - Cimacan","4068 - Kademangan","4071 - Kawungluwuk",
        "4072 - Muka","4074 - Sukagalih","4035 - Sukasari","4062 - Bojong","4075 - Sukanagara","4077 - Cihaur",
        "4078 - Cikaroya","4084 - Cidadap","8094 - Gekbrong"
    )

    val ppbk = arrayOf(
        "A Sulaeman Ss","Abdul Azzis Muslim","M Akbarulloh","Muhammad Khairul Farhan","Muhammad Taufiq Ismail","Neng Rahayu Novita"
    )

    val reportTypes = arrayOf(
        "Aktivasi Agen Baru","Reaktivasi Agen","Akuisisi Agen Baru","Tambah Alat","BEP","Juragan","Jawara",
        "Sales Volume & Transaksi","Transaksi eSMIK","Kunjungan Agen Fee Tunai","Kunjungan Agen CASA Tunai",
        "Usulan Debitur BRILink","Referral Senyum Mobile"
    )

    val acquisitionSources = arrayOf("Referral Unit","SRC","BRIMOLA","BUMDes","KUD/Koperasi","Indogrosir","PNM")
    val fundingSources = arrayOf("Transaksi Agen","Dana Talangan Bank Raya")

    fun needsAcquisition(type: String) = type == "Akuisisi Agen Baru" || type == "Tambah Alat"
    fun needsSalesVolume(type: String) = type == "Sales Volume & Transaksi"
}
