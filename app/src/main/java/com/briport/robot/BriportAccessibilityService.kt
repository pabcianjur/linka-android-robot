package com.briport.robot

import android.accessibilityservice.AccessibilityService
import android.content.*
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.provider.MediaStore
import android.content.ContentValues

class BriportAccessibilityService : AccessibilityService() {
    companion object { @Volatile var startRequested=false }
    private val handler=Handler(Looper.getMainLooper())
    private var lastAction=0L
    private var photoReady=false
    private var pickerOpened=false
    private var submitted=false

    override fun onServiceConnected(){ sendStatus("🤖 BRIPORT Robot aktif.") }

    override fun onAccessibilityEvent(event:android.view.accessibility.AccessibilityEvent?) {
        if(!startRequested) return
        val root=rootInActiveWindow ?: return
        val pkg=event?.packageName?.toString() ?: ""
        val prefs=getSharedPreferences(MainActivity.PREFS,MODE_PRIVATE)
        val jobId=prefs.getString("jobId",null) ?: return
        val fileName=prefs.getString("jobFileName",null) ?: ""

        if(pkg.contains("documentsui") || pkg.contains("providers.media") || pkg.contains("filemanager")) {
            handlePicker(root,fileName); return
        }
        if(pkg!="com.android.chrome") return
        val text=collectText(root).lowercase()

        if(text.contains("bukti struk transaksi") || text.contains("tambahkan file") || text.contains("upload file")) {
            if(!photoReady && !pickerOpened && System.currentTimeMillis()-lastAction>1500){
                val upload=findByText(root,listOf("Tambahkan file","Upload file","Add file"))
                if(upload!=null){ click(upload); pickerOpened=true; lastAction=System.currentTimeMillis(); sendStatus("📎 Membuka pemilih file..."); return }
            }
        }

        if(!photoReady && text.contains(fileName.lowercase()) && fileName.isNotBlank()) photoReady=true

        if(photoReady && (text.contains("kirim") || text.contains("submit")) && !submitted) {
            val send=findByText(root,listOf("Kirim","Submit"))
            if(send!=null){
                handler.postDelayed({ if(startRequested){ click(send); submitted=true; startRequested=false; sendStatus("✅ LAPORAN TERKIRIM. BRIPORT selesai.") } },1000)
                return
            }
        }

        if(!photoReady) {
            val next=findByText(root,listOf("Berikutnya","Next","Lanjut"))
            if(next!=null && System.currentTimeMillis()-lastAction>1200){ click(next); lastAction=System.currentTimeMillis(); sendStatus("➡️ Maju ke halaman berikutnya..."); return }
        }
        if(photoReady){
            val next=findByText(root,listOf("Berikutnya","Next","Lanjut"))
            if(next!=null && System.currentTimeMillis()-lastAction>1200){ click(next); lastAction=System.currentTimeMillis(); sendStatus("➡️ Foto terpasang. Menuju Kirim...") }
        }
    }

    private fun handlePicker(root:AccessibilityNodeInfo,fileName:String){
        if(fileName.isBlank()) return
        val target=findByText(root,listOf(fileName))
        if(target!=null && System.currentTimeMillis()-lastAction>1200){
            click(target); lastAction=System.currentTimeMillis(); handler.postDelayed({
                val r=rootInActiveWindow ?: return@postDelayed
                val open=findByText(r,listOf("Open","Buka","Pilih","Select")); if(open!=null) click(open)
                sendStatus("📎 File dipilih: $fileName")
            },800); return
        }
        val downloads=findByText(root,listOf("Downloads","Unduhan"))
        if(downloads!=null && System.currentTimeMillis()-lastAction>1500){ click(downloads); lastAction=System.currentTimeMillis(); return }
    }

    override fun onInterrupt(){ sendStatus("⏸️ Robot dihentikan sistem.") }

    private fun click(n:AccessibilityNodeInfo){ if(n.isClickable) n.performAction(AccessibilityNodeInfo.ACTION_CLICK) else n.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK) }
    private fun findByText(root:AccessibilityNodeInfo,labels:List<String>):AccessibilityNodeInfo?{ for(label in labels){ val ns=root.findAccessibilityNodeInfosByText(label); for(n in ns){ if(n.isVisibleToUser){ var p: AccessibilityNodeInfo? = n; var d=0; while(p!=null&&d<3){ if(p.isClickable) return p; p=p.parent; d++ } } } }; return null }
    private fun collectText(root:AccessibilityNodeInfo):String{ val b=StringBuilder(); fun w(n:AccessibilityNodeInfo?){if(n==null)return; n.text?.let{b.append(' ').append(it)}; n.contentDescription?.let{b.append(' ').append(it)}; for(i in 0 until n.childCount)w(n.getChild(i))}; w(root); return b.toString() }
    private fun sendStatus(msg:String){ sendBroadcast(Intent("com.briport.robot.STATUS").putExtra("status",msg)) }
}
