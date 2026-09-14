package com.briport.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo

class BriportAccessibilityService : AccessibilityService() {
    companion object { @Volatile var startRequested = false }
    private val handler = Handler(Looper.getMainLooper())
    private var lastAction = 0L
    private var selectedFile = false
    private var uploadOpened = false
    private var finished = false

    override fun onServiceConnected() { sendStatus("🤖 BRIPORT Robot siap.") }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        val prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
        val running = startRequested || prefs.getBoolean("running", false)
        if (!running || finished) return

        val root = rootInActiveWindow ?: return
        val pkg = event?.packageName?.toString() ?: ""
        val fileName = prefs.getString("jobFileName", "") ?: ""

        if (isPickerPackage(pkg)) { handlePicker(root, fileName); return }
        if (pkg != "com.android.chrome") return

        val pageText = collectText(root).lowercase()

        // Google Forms may render upload controls with text or content descriptions.
        if (!selectedFile && (pageText.contains("bukti struk transaksi") || pageText.contains("tambahkan file") || pageText.contains("add file") || pageText.contains("upload file"))) {
            if (!uploadOpened && cooldown()) {
                val upload = findClickable(root, listOf("Tambahkan file", "Add file", "Upload file"))
                    ?: findClickableByDescription(root, listOf("Tambahkan file", "Add file", "Upload file"))
                if (upload != null) {
                    click(upload); uploadOpened = true; markAction(); sendStatus("📎 Membuka pemilih file...")
                    return
                }
            }
        }

        if (!selectedFile && fileName.isNotBlank() && pageText.contains(fileName.lowercase())) {
            selectedFile = true
            sendStatus("📎 Bukti terpasang: $fileName")
        }

        if (selectedFile && !finished && cooldown()) {
            val send = findClickable(root, listOf("Kirim", "Submit", "Send"))
            if (send != null) {
                handler.postDelayed({
                    if (startRequested || getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).getBoolean("running", false)) {
                        click(send); finished = true; startRequested = false
                        getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).edit().putBoolean("running", false).apply()
                        sendStatus("✅ LAPORAN TERKIRIM. BRIPORT selesai.")
                    }
                }, 800)
                return
            }
        }

        if (!selectedFile && cooldown()) {
            val next = findClickable(root, listOf("Berikutnya", "Next", "Lanjut"))
            if (next != null) { click(next); markAction(); sendStatus("➡️ Mengisi halaman berikutnya..."); return }
        }

        if (selectedFile && cooldown()) {
            val next = findClickable(root, listOf("Berikutnya", "Next", "Lanjut"))
            if (next != null) { click(next); markAction(); sendStatus("➡️ Bukti terpasang. Menuju Kirim...") }
        }
    }

    private fun handlePicker(root: AccessibilityNodeInfo, fileName: String) {
        if (fileName.isBlank() || !cooldown()) return

        val target = findClickable(root, listOf(fileName))
        if (target != null) {
            click(target); markAction()
            handler.postDelayed({
                val r = rootInActiveWindow ?: return@postDelayed
                val open = findClickable(r, listOf("Open", "Buka", "Pilih", "Select"))
                if (open != null) { click(open); sendStatus("📎 File dipilih: $fileName") }
            }, 700)
            return
        }

        val briportFolder = findClickable(root, listOf("BRIPORT"))
        if (briportFolder != null) { click(briportFolder); markAction(); return }

        val downloads = findClickable(root, listOf("Downloads", "Unduhan"))
        if (downloads != null) { click(downloads); markAction() }
    }

    private fun isPickerPackage(pkg: String) = pkg.contains("documentsui") || pkg.contains("providers.media") || pkg.contains("filemanager") || pkg.contains("files")
    private fun cooldown() = System.currentTimeMillis() - lastAction > 1200
    private fun markAction() { lastAction = System.currentTimeMillis() }

    private fun click(node: AccessibilityNodeInfo) {
        if (node.isClickable) node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        else node.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun findClickable(root: AccessibilityNodeInfo, labels: List<String>): AccessibilityNodeInfo? {
        for (label in labels) {
            val nodes = root.findAccessibilityNodeInfosByText(label)
            for (node in nodes) {
                if (!node.isVisibleToUser) continue
                var p: AccessibilityNodeInfo? = node
                repeat(4) {
                    if (p?.isClickable == true) return p
                    p = p?.parent
                }
            }
        }
        return null
    }

    private fun findClickableByDescription(root: AccessibilityNodeInfo, labels: List<String>): AccessibilityNodeInfo? {
        fun walk(n: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (n == null) return null
            val d = n.contentDescription?.toString()?.lowercase() ?: ""
            if (n.isVisibleToUser && labels.any { d.contains(it.lowercase()) }) {
                if (n.isClickable) return n
                n.parent?.let { if (it.isClickable) return it }
            }
            for (i in 0 until n.childCount) walk(n.getChild(i))?.let { return it }
            return null
        }
        return walk(root)
    }

    private fun collectText(root: AccessibilityNodeInfo): String {
        val b = StringBuilder()
        fun walk(n: AccessibilityNodeInfo?) {
            if (n == null) return
            n.text?.let { b.append(' ').append(it) }
            n.contentDescription?.let { b.append(' ').append(it) }
            for (i in 0 until n.childCount) walk(n.getChild(i))
        }
        walk(root)
        return b.toString()
    }

    override fun onInterrupt() { sendStatus("⏸️ Robot dihentikan Android.") }

    private fun sendStatus(msg: String) {
        sendBroadcast(Intent("com.briport.STATUS").setPackage(packageName).putExtra("status", msg))
    }
}
