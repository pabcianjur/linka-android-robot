package com.briport.robot

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.MediaStore
import android.content.ContentValues
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    companion object {
        const val PREFS = "briport"
        const val API_URL = "https://script.google.com/macros/s/AKfycbyRNEUAaYN9ld8Ly9EOFxMpaQw3sTL0EXfhjjoCdMF58S1p_M7x-eCbRYwsGOv2ovuk/exec"
    }

    private lateinit var status: TextView
    private lateinit var branch: Spinner
    private lateinit var unit: Spinner
    private lateinit var ppbk: Spinner
    private lateinit var jenis: Spinner
    private lateinit var outlet: EditText
    private lateinit var agen: EditText
    private lateinit var source: Spinner
    private lateinit var sourceDana: Spinner
    private lateinit var nominal: EditText
    private lateinit var trxKembali: EditText
    private lateinit var frekuensi: EditText
    private var photoFile: File? = null

    private val units = arrayOf(
        "4070 - Kadupandak","4073 - Saganten","4076 - Tanggeung","4086 - Pagelaran","7471 - Agrabinta","7732 - Cibinong","4063 - Bojongpicung","4067 - Cipetir","4069 - Ciranjang Baru","4079 - Ciranjang","4080 - Pamoyanan","4081 - Sawahgede","3453 - Cipanas","4064 - Ciherang","4065 - Cijedil","4066 - Cimacan","4068 - Kademangan","4071 - Kawungluwuk","4072 - Muka","4074 - Sukagalih","4035 - Sukasari","4062 - Bojong","4075 - Sukanagara","4077 - Cihaur","4078 - Cikaroya","4084 - Cidadap","8094 - Gekbrong"
    )
    private val ppbks = arrayOf("A Sulaeman Ss","Abdul Azzis Muslim","M Akbarulloh","Muhammad Khairul Farhan","Muhammad Taufiq Ismail","Neng Rahayu Novita")
    private val types = arrayOf("Aktivasi Agen Baru","Reaktivasi Agen","Akuisisi Agen Baru","Tambah Alat","BEP","Juragan","Jawara","Sales Volume & Transaksi","Transaksi eSMIK","Kunjungan Agen Fee Tunai","Kunjungan Agen CASA Tunai","Usulan Debitur BRILink","Referral Senyum Mobile")
    private val sourceChoices = arrayOf("Referral Unit","SRC","BRIMOLA","BUMDes","KUD/Koperasi","Indogrosir","PNM")
    private val danaChoices = arrayOf("Transaksi Agen","Dana Talangan Bank Raya")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); handleIntent(intent) }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "briport" && uri.host == "run") {
            val jobId = uri.getQueryParameter("jobId") ?: return
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("jobId", jobId).apply()
            startRobotFromJob(jobId)
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28,28,28,28) }
        fun label(t:String) = TextView(this).apply { text=t; textSize=14f; setPadding(0,14,0,5) }
        fun edit(hint:String, value:String="") = EditText(this).apply { this.hint=hint; setText(value); textSize=15f }
        fun spinner(items:Array<String>) = Spinner(this).apply { adapter=ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items) }

        root.addView(TextView(this).apply { text="BRIPORT Robot"; textSize=28f; setTextColor(0xFF0B4EA2.toInt()); gravity=Gravity.CENTER_VERTICAL })
        root.addView(TextView(this).apply { text="Pelaporan BRILink • FINAL"; textSize=15f })

        root.addView(label("Branch Office")); branch=spinner(arrayOf("Cianjur")); root.addView(branch)
        root.addView(label("Unit Kerja")); unit=spinner(units); root.addView(unit)
        root.addView(label("Role")); root.addView(TextView(this).apply { text="PPBK"; textSize=16f })
        root.addView(label("Nama PPBK")); ppbk=spinner(ppbks); ppbk.setSelection(1); root.addView(ppbk)
        root.addView(label("Jenis Pelaporan")); jenis=spinner(types); root.addView(jenis)
        root.addView(label("ID Outlet")); outlet=edit("ID Outlet"); root.addView(outlet)
        root.addView(label("Nama Agen")); agen=edit("Nama Agen"); root.addView(agen)

        root.addView(label("Sumber Akuisisi / Tambah Alat (jika diperlukan)")); source=spinner(sourceChoices); root.addView(source)
        root.addView(label("Sumber Dana (Sales Volume & Transaksi)")); sourceDana=spinner(danaChoices); root.addView(sourceDana)
        root.addView(label("Nominal Pencairan Dana Talangan")); nominal=edit("Nominal"); root.addView(nominal)
        root.addView(label("Total Nominal Trx Kembali ke BRILink")); trxKembali=edit("Total nominal"); root.addView(trxKembali)
        root.addView(label("Berapa kali ditransaksikan kembali")); frekuensi=edit("Frekuensi"); root.addView(frekuensi)

        val photoBtn=Button(this).apply { text="📷 PILIH / AMBIL FOTO"; setOnClickListener { choosePhoto() } }; root.addView(photoBtn)
        val run=Button(this).apply { text="🚀 MULAI PELAPORAN"; setOnClickListener { prepareLocalJobAndOpen() } }; root.addView(run)
        val settings=Button(this).apply { text="⚙ AKTIFKAN AKSESIBILITAS ROBOT"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }; root.addView(settings)
        status=TextView(this).apply { text="Siap. PWA juga dapat membuka aplikasi ini otomatis lewat briport://run."; textSize=14f; setPadding(16,18,16,18); setBackgroundColor(0xFFF1F6FB.toInt()) }; root.addView(status)
        root.addView(TextView(this).apply { text="\nCatatan: Robot akan mengisi GForm resmi, upload bukti, lalu menekan KIRIM setelah file terpasang. Jangan gunakan aplikasi bersamaan dengan sentuhan manual pada Chrome."; textSize=12f })
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun choosePhoto() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type="image/*"; addCategory(Intent.CATEGORY_OPENABLE) }, 100)
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode!=100 || resultCode!=RESULT_OK || data?.data==null) return
        val uri=data.data!!
        val name=queryName(uri) ?: "BRIPORT_Bukti.jpg"
        val file=File(cacheDir, "briport_${System.currentTimeMillis()}_${name.replace(Regex("[^A-Za-z0-9._-]"),"_")}")
        contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(file).use { out -> input.copyTo(out) } }
        photoFile=file
        getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("photoPath",file.absolutePath).putString("photoName",file.name).apply()
        status.text="📎 Foto siap: ${file.name}"
    }

    private fun queryName(uri:Uri):String? {
        contentResolver.query(uri,null,null,null,null)?.use { c -> if(c.moveToFirst()){ val i=c.getColumnIndex("_display_name"); if(i>=0) return c.getString(i) } }
        return null
    }

    private fun prepareLocalJobAndOpen() {
        val file=photoFile ?: getSharedPreferences(PREFS,MODE_PRIVATE).getString("photoPath",null)?.let{File(it)}
        if(file==null || !file.exists()){ status.text="⚠️ Pilih foto terlebih dahulu."; return }
        val data=JSONObject().apply {
            put("branchOffice", "Cianjur"); put("unitKerja", unit.selectedItem.toString()); put("role","PPBK"); put("namaPPBK",ppbk.selectedItem.toString()); put("jenisPelaporan",jenis.selectedItem.toString()); put("idOutlet",outlet.text.toString()); put("namaAgen",agen.text.toString()); put("sumberAkuisisi",source.selectedItem.toString()); put("sumberDana",sourceDana.selectedItem.toString()); put("nominalPencairan",nominal.text.toString()); put("trxKembali",trxKembali.text.toString()); put("frekuensiKembali",frekuensi.text.toString())
        }
        status.text="⏳ Mengirim data ke backend..."
        Thread { try { val job=createJob(data,file); runOnUiThread{ status.text="🤖 Job siap. Membuka GForm..."; startRobotFromJob(job) } } catch(e:Exception){ runOnUiThread{status.text="❌ ${e.message}"} } }.start()
    }

    private fun createJob(data:JSONObject,file:File):String {
        val b64=android.util.Base64.encodeToString(file.readBytes(),android.util.Base64.NO_WRAP)
        val p=JSONObject(data.toString()).apply{ put("mode","FINAL"); put("fileName",file.name); put("mimeType",mime(file.name)); put("fileBase64",b64) }
        val conn=(URL(API_URL).openConnection() as HttpURLConnection).apply{ requestMethod="POST"; doOutput=true; connectTimeout=30000; readTimeout=60000; setRequestProperty("Content-Type","text/plain;charset=utf-8") }
        conn.outputStream.use{it.write(p.toString().toByteArray())}
        val body=conn.inputStream.bufferedReader().readText(); val r=JSONObject(body); if(!r.optBoolean("success")) throw Exception(r.optString("message","Backend gagal")); return r.getJSONObject("data").getString("jobId")
    }

    private fun startRobotFromJob(jobId:String) {
        getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("jobId",jobId).putBoolean("running",true).apply()
        BriportAccessibilityService.startRequested=true
        Thread { try {
            val json=fetchJson("$API_URL?action=job&jobId=${Uri.encode(jobId)}")
            val prefill=json.getString("prefilledUrl")
            val fileName=json.optString("fileName","BRIPORT_Bukti.jpg")
            getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("prefilledUrl",prefill).putString("jobFileName",fileName).apply()
            val fileJson=fetchJson("$API_URL?action=file&jobId=${Uri.encode(jobId)}")
            val bytes=android.util.Base64.decode(fileJson.getString("base64"),android.util.Base64.DEFAULT)
            saveToDownloads(fileName, fileJson.optString("mimeType","image/jpeg"), bytes)
            runOnUiThread { startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(prefill))) }
        } catch(e:Exception) { runOnUiThread { status.text="❌ Gagal mengambil job: ${e.message}" } } } .start()
    }

    private fun saveToDownloads(name:String,mime:String,bytes:ByteArray){
        val values=android.content.ContentValues().apply{ put(MediaStore.Downloads.DISPLAY_NAME,name); put(MediaStore.Downloads.MIME_TYPE,mime); put(MediaStore.Downloads.RELATIVE_PATH,"Download/BRIPORT") }
        val uri=contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values) ?: throw Exception("Tidak bisa menyimpan foto ke Downloads")
        contentResolver.openOutputStream(uri)?.use{it.write(bytes)} ?: throw Exception("Tidak bisa menulis foto")
    }

    private fun fetchJson(url:String):JSONObject {
        val c=(URL(url).openConnection() as HttpURLConnection).apply{ requestMethod="GET"; connectTimeout=30000; readTimeout=60000 }
        val body=c.inputStream.bufferedReader().readText(); val j=JSONObject(body); if(!j.optBoolean("success")) throw Exception(j.optString("message","Backend gagal")); return j
    }

    private fun mime(name:String)=when(name.lowercase().substringAfterLast('.',"")){"png"->"image/png";"webp"->"image/webp";else->"image/jpeg"}
}
