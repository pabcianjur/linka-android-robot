package com.briport.app

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    companion object {
        const val PREFS = "briport"
        const val API_URL = "https://script.google.com/macros/s/AKfycbyRNEUAaYN9ld8Ly9EOFxMpaQw3sTL0EXfhjjoCdMF58S1p_M7x-eCbRYwsGOv2ovuk/exec"
        const val SCHEME = "briport"
    }

    private lateinit var status: TextView
    private lateinit var unit: Spinner
    private lateinit var ppbk: Spinner
    private lateinit var jenis: Spinner
    private lateinit var outlet: EditText
    private lateinit var agen: EditText
    private lateinit var acquisition: Spinner
    private lateinit var funding: Spinner
    private lateinit var nominal: EditText
    private lateinit var trxKembali: EditText
    private lateinit var frekuensi: EditText
    private lateinit var optionalBox: LinearLayout
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        ContextCompat.registerReceiver(this, statusReceiver, IntentFilter("com.briport.STATUS"), ContextCompat.RECEIVER_NOT_EXPORTED)
        handleIntent(intent)
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(statusReceiver) }
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            status.text = intent?.getStringExtra("status") ?: ""
        }
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == SCHEME && uri.host == "run") {
            val jobId = uri.getQueryParameter("jobId") ?: return
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("jobId", jobId).putBoolean("running", true).apply()
            startRobotFromJob(jobId)
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 28, 28, 28) }
        fun label(t: String) = TextView(this).apply { text = t; textSize = 14f; setPadding(0, 14, 0, 5) }
        fun edit(hint: String) = EditText(this).apply { this.hint = hint; textSize = 15f }
        fun spinner(items: Array<String>) = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items)
        }

        root.addView(TextView(this).apply { text = "BRIPORT"; textSize = 30f; setTextColor(0xFF0B4EA2.toInt()); gravity = Gravity.CENTER_VERTICAL })
        root.addView(TextView(this).apply { text = "See. Track. Act. • Pelaporan Aktivitas BRILink"; textSize = 14f })

        root.addView(label("Branch Office")); root.addView(TextView(this).apply { text = "Cianjur"; textSize = 16f })
        root.addView(label("Unit Kerja")); unit = spinner(ReportCatalog.units); root.addView(unit)
        root.addView(label("Role")); root.addView(TextView(this).apply { text = "PPBK"; textSize = 16f })
        root.addView(label("Nama PPBK")); ppbk = spinner(ReportCatalog.ppbk); ppbk.setSelection(1); root.addView(ppbk)
        root.addView(label("Jenis Pelaporan")); jenis = spinner(ReportCatalog.reportTypes); root.addView(jenis)
        root.addView(label("ID Outlet")); outlet = edit("ID Outlet"); root.addView(outlet)
        root.addView(label("Nama Agen")); agen = edit("Nama Agen"); root.addView(agen)

        optionalBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        optionalBox.addView(label("Sumber Akuisisi / Tambah Alat")); acquisition = spinner(ReportCatalog.acquisitionSources); optionalBox.addView(acquisition)
        optionalBox.addView(label("Sumber Dana")); funding = spinner(ReportCatalog.fundingSources); optionalBox.addView(funding)
        optionalBox.addView(label("Nominal Pencairan Dana Talangan")); nominal = edit("Nominal"); optionalBox.addView(nominal)
        optionalBox.addView(label("Total Nominal Trx Kembali ke BRILink")); trxKembali = edit("Total nominal"); optionalBox.addView(trxKembali)
        optionalBox.addView(label("Berapa kali ditransaksikan kembali")); frekuensi = edit("Frekuensi"); optionalBox.addView(frekuensi)
        root.addView(optionalBox)

        jenis.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { updateOptionalFields() }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        updateOptionalFields()

        root.addView(label("Bukti Foto"))
        val photoBtn = Button(this).apply { text = "📷 PILIH FOTO"; setOnClickListener { choosePhoto() } }
        root.addView(photoBtn)

        val run = Button(this).apply { text = "🚀 MULAI PELAPORAN"; setOnClickListener { startManualJob() } }
        root.addView(run)
        val stop = Button(this).apply { text = "⏹ HENTIKAN ROBOT"; setOnClickListener { stopRobot() } }
        root.addView(stop)
        val settings = Button(this).apply { text = "⚙ AKTIFKAN AKSES ROBOT"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }
        root.addView(settings)

        status = TextView(this).apply { text = "Siap."; textSize = 14f; setPadding(16, 18, 16, 18); setBackgroundColor(0xFFF1F6FB.toInt()) }
        root.addView(status)
        root.addView(TextView(this).apply {
            text = "Robot hanya berjalan setelah Anda menekan MULAI PELAPORAN. Aksesibilitas harus diaktifkan manual oleh pengguna. BRIPORT tidak menyembunyikan atau melewati kontrol keamanan Android."
            textSize = 12f
            setPadding(0, 16, 0, 0)
        })

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun updateOptionalFields() {
        val type = jenis.selectedItem?.toString() ?: ""
        val showAcq = ReportCatalog.needsAcquisition(type)
        val showSales = ReportCatalog.needsSalesVolume(type)
        for (i in 0 until optionalBox.childCount) optionalBox.getChildAt(i).visibility = View.GONE
        if (showAcq) {
            optionalBox.getChildAt(0).visibility = View.VISIBLE
            optionalBox.getChildAt(1).visibility = View.VISIBLE
        }
        if (showSales) for (i in 2 until optionalBox.childCount) optionalBox.getChildAt(i).visibility = View.VISIBLE
    }

    private fun choosePhoto() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"; addCategory(Intent.CATEGORY_OPENABLE); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 100 || resultCode != Activity.RESULT_OK || data?.data == null) return
        val uri = data.data!!
        val name = queryName(uri) ?: "BRIPORT_Bukti.jpg"
        val file = File(cacheDir, "briport_${System.currentTimeMillis()}_${name.replace(Regex("[^A-Za-z0-9._-]"), "_")}")
        contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(file).use { output -> input.copyTo(output) } }
        photoFile = file
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("photoPath", file.absolutePath).putString("photoName", file.name).apply()
        status.text = "📎 Foto siap: ${file.name}"
    }

    private fun queryName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val i = c.getColumnIndex("_display_name")
                if (i >= 0) return c.getString(i)
            }
        }
        return null
    }

    private fun startManualJob() {
        val file = photoFile ?: getSharedPreferences(PREFS, MODE_PRIVATE).getString("photoPath", null)?.let(::File)
        if (file == null || !file.exists()) { status.text = "⚠️ Pilih foto terlebih dahulu."; return }
        val payload = JSONObject().apply {
            put("mode", "FINAL")
            put("branchOffice", "Cianjur")
            put("unitKerja", unit.selectedItem.toString())
            put("role", "PPBK")
            put("namaPPBK", ppbk.selectedItem.toString())
            put("jenisPelaporan", jenis.selectedItem.toString())
            put("idOutlet", outlet.text.toString().trim())
            put("namaAgen", agen.text.toString().trim())
            put("sumberAkuisisi", acquisition.selectedItem.toString())
            put("sumberDana", funding.selectedItem.toString())
            put("nominalPencairan", nominal.text.toString().trim())
            put("trxKembali", trxKembali.text.toString().trim())
            put("frekuensiKembali", frekuensi.text.toString().trim())
        }
        if (payload.getString("idOutlet").isBlank() || payload.getString("namaAgen").isBlank()) { status.text = "⚠️ ID Outlet dan Nama Agen wajib diisi."; return }
        status.text = "⏳ Menyiapkan laporan..."
        Thread {
            try {
                val b64 = android.util.Base64.encodeToString(file.readBytes(), android.util.Base64.NO_WRAP)
                payload.put("fileName", file.name); payload.put("mimeType", mime(file.name)); payload.put("fileBase64", b64)
                val result = postJson(payload)
                val jobId = result.getJSONObject("data").getString("jobId")
                runOnUiThread { startRobotFromJob(jobId) }
            } catch (e: Exception) { runOnUiThread { status.text = "❌ ${e.message ?: "Gagal menyiapkan laporan"}" } }
        }.start()
    }

    private fun startRobotFromJob(jobId: String) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("jobId", jobId).putBoolean("running", true).apply()
        Thread {
            try {
                val job = getJson("$API_URL?action=job&jobId=${Uri.encode(jobId)}")
                val prefill = job.getString("prefilledUrl")
                val fileName = job.optString("fileName", "BRIPORT_Bukti.jpg")
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("jobFileName", fileName).putString("prefilledUrl", prefill).apply()
                val fileJson = getJson("$API_URL?action=file&jobId=${Uri.encode(jobId)}")
                saveToDownloads(fileName, fileJson.optString("mimeType", "image/jpeg"), android.util.Base64.decode(fileJson.getString("base64"), android.util.Base64.DEFAULT))
                runOnUiThread {
                    status.text = "🤖 BRIPORT Robot aktif. Membuka GForm resmi..."
                    BriportAccessibilityService.startRequested = true
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(prefill)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            } catch (e: Exception) { runOnUiThread { status.text = "❌ Gagal mengambil job: ${e.message}" } }
        }.start()
    }

    private fun stopRobot() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean("running", false).apply()
        BriportAccessibilityService.startRequested = false
        status.text = "⏹ Robot dihentikan."
    }

    private fun saveToDownloads(name: String, mime: String, bytes: ByteArray) {
        val resolver = contentResolver
        val values = ContentValues().apply {
            put(android.provider.MediaStore.Downloads.DISPLAY_NAME, name)
            put(android.provider.MediaStore.Downloads.MIME_TYPE, mime)
            put(android.provider.MediaStore.Downloads.RELATIVE_PATH, "Download/BRIPORT")
            put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: throw Exception("Tidak bisa menyiapkan file bukti")
        try {
            resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: throw Exception("Tidak bisa menulis file bukti")
            resolver.update(uri, ContentValues().apply { put(android.provider.MediaStore.Downloads.IS_PENDING, 0) }, null, null)
        } catch (e: Exception) { resolver.delete(uri, null, null); throw e }
    }

    private fun postJson(payload: JSONObject): JSONObject {
        val c = (URL(API_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; doOutput = true; connectTimeout = 30000; readTimeout = 90000
            setRequestProperty("Content-Type", "text/plain;charset=utf-8")
        }
        c.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
        val body = c.inputStream.bufferedReader().readText(); val j = JSONObject(body)
        if (!j.optBoolean("success")) throw Exception(j.optString("message", "Backend gagal"))
        return j
    }

    private fun getJson(url: String): JSONObject {
        val c = (URL(url).openConnection() as HttpURLConnection).apply { requestMethod = "GET"; connectTimeout = 30000; readTimeout = 90000 }
        val body = c.inputStream.bufferedReader().readText(); val j = JSONObject(body)
        if (!j.optBoolean("success")) throw Exception(j.optString("message", "Backend gagal"))
        return j
    }

    private fun mime(name: String) = when (name.lowercase().substringAfterLast('.', "")) {
        "png" -> "image/png"; "webp" -> "image/webp"; "heic", "heif" -> "image/heic"; else -> "image/jpeg"
    }
}
