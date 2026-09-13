package com.linka.robot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            status.text = intent?.getStringExtra("status") ?: "Status tidak tersedia."
        }
    }

    // Official responder URL + prefilled values discovered for this form.
    private val formUrl = "https://docs.google.com/forms/d/e/1FAIpQLSdlrpM1bmH9NmLTvk28V4QOZPLqiZ3Qa02agjTFea8_bKcdg/viewform" +
            "?entry.277681540=Cianjur" +
            "&entry.1562051279=4063%20-%20Bojongpicung" +
            "&entry.711732499=PPBK" +
            "&entry.299784048=Abdul%20Azzis%20Muslim" +
            "&entry.2139012705=Reaktivasi%20Agen" +
            "&entry.6046308004=11223344" +
            "&entry.2048748380=Kirana%20cell"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(receiver, IntentFilter("com.linka.robot.STATUS"), RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        unregisterReceiver(receiver)
        super.onStop()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 44, 36, 36)
        }

        val title = TextView(this).apply {
            text = "LINKA Robot TEST"
            textSize = 28f
            setTextColor(0xFF1456A0.toInt())
            setPadding(0, 0, 0, 8)
        }
        root.addView(title)

        val info = TextView(this).apply {
            text = "Tahap 1: buka GForm prefilled dan robot maju antar halaman. Robot WAJIB berhenti sebelum tombol KIRIM.\n\nAktifkan layanan aksesibilitas LINKA Robot terlebih dahulu."
            textSize = 16f
            setPadding(0, 0, 0, 24)
        }
        root.addView(info)

        val settings = Button(this).apply {
            text = "1. AKTIFKAN ROBOT"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        root.addView(settings)

        val open = Button(this).apply {
            text = "2. MULAI ROBOT TEST"
            setOnClickListener { openForm() }
        }
        root.addView(open)

        val statusTitle = TextView(this).apply {
            text = "Status"
            textSize = 20f
            setPadding(0, 28, 0, 8)
        }
        root.addView(statusTitle)

        status = TextView(this).apply {
            text = "Siap. Robot belum dijalankan."
            textSize = 15f
            setBackgroundColor(0xFFF2F6FA.toInt())
            setPadding(18, 18, 18, 18)
        }
        root.addView(status)

        val note = TextView(this).apply {
            text = "\nTEST MODE:\n• Tidak menekan KIRIM\n• Tidak membuat submission baru\n• Tujuan hanya menguji kontrol halaman GForm\n• Jika robot gagal menemukan elemen, status akan menunjukkan langkah terakhir"
            textSize = 14f
        }
        root.addView(note)

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun openForm() {
        LinkaAccessibilityService.startRequested = true
        status.text = "Membuka GForm..."
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formUrl))
        startActivity(intent)
    }
}
