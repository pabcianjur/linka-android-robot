package com.linka.robot

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo
import android.content.Intent

class LinkaAccessibilityService : AccessibilityService() {
    companion object {
        @Volatile var startRequested = false
    }

    private val handler = Handler(Looper.getMainLooper())
    private var lastActionAt = 0L
    private var pageAdvanceCount = 0

    override fun onServiceConnected() {
        sendStatus("🤖 Robot aktif. Buka GForm melalui tombol MULAI ROBOT TEST.")
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        if (!startRequested) return
        val root = rootInActiveWindow ?: return
        val pkg = event?.packageName?.toString() ?: ""
        if (pkg != "com.android.chrome") return

        val allText = collectText(root).lowercase()

        if (allText.contains("kirim") || allText.contains("submit")) {
            // Safety lock: never press the final submit button in this test build.
            sendStatus("🛑 ROBOT BERHENTI: halaman sudah mencapai area KIRIM. Tidak ada submission yang dibuat.")
            startRequested = false
            return
        }

        // Google Forms usually uses 'Berikutnya' on section pages.
        val next = findNodeByText(root, listOf("Berikutnya", "Next", "Lanjut"))
        if (next != null && System.currentTimeMillis() - lastActionAt > 1200) {
            val clicked = next.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (clicked) {
                lastActionAt = System.currentTimeMillis()
                pageAdvanceCount++
                sendStatus("🤖 Robot menekan Berikutnya. Halaman maju: $pageAdvanceCount")
                return
            }
        }

        if (allText.contains("bukti struk transaksi") || allText.contains("upload file") || allText.contains("tambahkan file")) {
            sendStatus("📎 Robot mencapai bagian upload bukti. TEST 1 selesai — berhenti sebelum upload/Kirim.")
            startRequested = false
            return
        }

        sendStatus("👀 Robot membaca halaman GForm... menunggu elemen berikutnya.")
    }

    override fun onInterrupt() {
        sendStatus("⏸️ Layanan robot dihentikan oleh sistem.")
    }

    private fun findNodeByText(root: AccessibilityNodeInfo, labels: List<String>): AccessibilityNodeInfo? {
        for (label in labels) {
            val nodes = root.findAccessibilityNodeInfosByText(label)
            for (node in nodes) {
                if (node.isVisibleToUser && node.isClickable) return node
                var p = node.parent
                var depth = 0
                while (p != null && depth < 3) {
                    if (p.isVisibleToUser && p.isClickable) return p
                    p = p.parent
                    depth++
                }
            }
        }
        return null
    }

    private fun collectText(root: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        fun walk(node: AccessibilityNodeInfo?) {
            if (node == null) return
            node.text?.let { sb.append(' ').append(it) }
            node.contentDescription?.let { sb.append(' ').append(it) }
            for (i in 0 until node.childCount) walk(node.getChild(i))
        }
        walk(root)
        return sb.toString()
    }

    private fun sendStatus(message: String) {
        val i = Intent("com.linka.robot.STATUS").putExtra("status", message)
        sendBroadcast(i)
    }
}
