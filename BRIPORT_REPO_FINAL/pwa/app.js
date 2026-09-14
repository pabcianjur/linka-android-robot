const BACKEND = "https://script.google.com/macros/s/AKfycbyRNEUAaYN9ld8Ly9EOFxMpaQw3sTL0EXfhjjoCdMF58S1p_M7x-eCbRYwsGOv2ovuk/exec";
const APP_SCHEME = "briport://run?jobId=";

const Uker = [
  ["4070","4070 - Kadupandak"],["4073","4073 - Saganten"],["4076","4076 - Tanggeung"],
  ["4086","4086 - Pagelaran"],["7471","7471 - Agrabinta"],["7732","7732 - Cibinong"],
  ["4063","4063 - Bojongpicung"],["4067","4067 - Cipetir"],["4069","4069 - Ciranjang Baru"],
  ["4079","4079 - Ciranjang"],["4080","4080 - Pamoyanan"],["4081","4081 - Sawahgede"],
  ["3453","3453 - Cipanas"],["4064","4064 - Ciherang"],["4065","4065 - Cijedil"],
  ["4066","4066 - Cimacan"],["4068","4068 - Kademangan"],["4071","4071 - Kawungluwuk"],
  ["4072","4072 - Muka"],["4074","4074 - Sukagalih"],["4035","4035 - Sukasari"],
  ["4062","4062 - Bojong"],["4075","4075 - Sukanagara"],["4077","4077 - Cihaur"],
  ["4078","4078 - Cikaroya"],["4084","4084 - Cidadap"],["8094","8094 - Gekbrong"]
];

const PPBK = [
  "A Sulaeman Ss",
  "Abdul Azzis Muslim",
  "M Akbarulloh",
  "Muhammad Khairul Farhan",
  "Muhammad Taufiq Ismail",
  "Neng Rahayu Novita"
];

const JENIS = [
  "Aktivasi Agen Baru","Reaktivasi Agen","Akuisisi Agen Baru","Tambah Alat","BEP",
  "Juragan","Jawara","Sales Volume & Transaksi","Transaksi eSMIK",
  "Kunjungan Agen Fee Tunai","Kunjungan Agen CASA Tunai","Usulan Debitur BRILink",
  "Referral Senyum Mobile"
];

let selectedPhoto = null;
let lastJobId = "";

function $(id){ return document.getElementById(id); }

function fillSelect(id, items){
  const el=$(id);
  items.forEach(item=>{
    const opt=document.createElement("option");
    if(Array.isArray(item)){
      opt.value=item[1];
      opt.textContent=item[1];
    }else{
      opt.value=item;
      opt.textContent=item;
    }
    el.appendChild(opt);
  });
}

function setStatus(message,type=""){
  const el=$("status");
  el.textContent=message;
  el.className="status "+type;
}

function openGallery(){
  $("galleryInput").click();
}

function openCamera(){
  $("cameraInput").click();
}

function handlePhoto(file){
  if(!file) return;

  if(!file.type.startsWith("image/")){
    setStatus("⚠️ File harus berupa gambar.","error");
    return;
  }

  selectedPhoto=file;
  $("photoName").textContent="Foto dipilih: "+file.name;

  const preview=$("preview");
  if(preview.dataset.url) URL.revokeObjectURL(preview.dataset.url);
  const url=URL.createObjectURL(file);
  preview.dataset.url=url;
  preview.src=url;
  preview.classList.remove("hidden");

  updateSummary();
  setStatus("Foto siap digunakan. Silakan tekan MULAI PELAPORAN.","");
}

$("galleryInput").addEventListener("change",function(){
  handlePhoto(this.files[0]);
});

$("cameraInput").addEventListener("change",function(){
  handlePhoto(this.files[0]);
});

function updateSummary(){
  $("summary").textContent =
`Branch Office : Cianjur
Unit Kerja    : ${$("unitKerja").value || "-"}
Role          : PPBK
Nama PPBK     : ${$("namaPPBK").value || "-"}
Jenis         : ${$("jenisPelaporan").value || "-"}
ID Outlet     : ${$("idOutlet").value || "-"}
Nama Agen     : ${$("namaAgen").value || "-"}
Foto          : ${selectedPhoto ? selectedPhoto.name : "-"}`;
}

["unitKerja","namaPPBK","jenisPelaporan","idOutlet","namaAgen","sumberAkuisisi","sumberDana","nominalPencairan","trxKembali","frekuensiKembali"].forEach(id=>{
  $(id).addEventListener("input",updateSummary);
  $(id).addEventListener("change",updateSummary);
});

function validate(){
  if(!$("unitKerja").value) return "Pilih Unit Kerja terlebih dahulu.";
  if(!$("namaPPBK").value) return "Pilih Nama PPBK terlebih dahulu.";
  if(!$("jenisPelaporan").value) return "Pilih Jenis Pelaporan terlebih dahulu.";
  if(!$("idOutlet").value.trim()) return "Isi ID Outlet terlebih dahulu.";
  if(!$("namaAgen").value.trim()) return "Isi Nama Agen terlebih dahulu.";
  if(!selectedPhoto) return "Pilih foto dari galeri atau ambil foto terlebih dahulu.";
  return "";
}

async function compressImage(file, maxSide=1800, quality=0.82){
  if(file.size <= 2.5*1024*1024 && /jpeg|jpg|png|webp/i.test(file.type)) return file;
  const bitmap = await createImageBitmap(file);
  const scale = Math.min(1, maxSide / Math.max(bitmap.width, bitmap.height));
  const canvas = document.createElement("canvas");
  canvas.width = Math.max(1, Math.round(bitmap.width*scale));
  canvas.height = Math.max(1, Math.round(bitmap.height*scale));
  const ctx = canvas.getContext("2d", {alpha:false});
  ctx.drawImage(bitmap, 0, 0, canvas.width, canvas.height);
  bitmap.close();
  const blob = await new Promise(resolve => canvas.toBlob(resolve, "image/jpeg", quality));
  if(!blob) throw new Error("Foto gagal dikompres.");
  return new File([blob], file.name.replace(/\.[^.]+$/, "")+".jpg", {type:"image/jpeg", lastModified:Date.now()});
}

function fileToBase64(file){
  return new Promise((resolve,reject)=>{
    const reader=new FileReader();
    reader.onload=()=>{
      const result=String(reader.result||"");
      const comma=result.indexOf(",");
      resolve(comma>=0?result.substring(comma+1):result);
    };
    reader.onerror=()=>reject(new Error("Foto gagal dibaca oleh HP."));
    reader.readAsDataURL(file);
  });
}

async function openRobot(jobId){
  lastJobId=jobId;
  const url=APP_SCHEME+encodeURIComponent(jobId);
  try { window.location.href=url; } catch(e) {}
  const btn=$("openRobotBtn");
  if(btn){ btn.classList.remove("hidden"); btn.onclick=()=>{ window.location.href=url; }; }
}

async function submitReport(){
  const btn=$("submitBtn");
  const error=validate(); updateSummary();
  if(error){ setStatus("⚠️ "+error,"error"); return; }
  if(selectedPhoto.size>8*1024*1024){ setStatus("⚠️ Ukuran foto terlalu besar. Maksimal 8 MB.","error"); return; }
  btn.disabled=true; btn.textContent="MENYIAPKAN ROBOT..."; setStatus("⏳ Menyimpan data + foto...","loading");
  try{
    const preparedPhoto=await compressImage(selectedPhoto);
    if(preparedPhoto.size>6*1024*1024) throw new Error("Foto masih terlalu besar setelah kompresi. Gunakan foto yang lebih ringan.");
    const base64=await fileToBase64(preparedPhoto);
    const payload={
      mode:"FINAL", branchOffice:"Cianjur", unitKerja:$("unitKerja").value, role:"PPBK",
      namaPPBK:$("namaPPBK").value, jenisPelaporan:$("jenisPelaporan").value,
      idOutlet:$("idOutlet").value.trim(), namaAgen:$("namaAgen").value.trim(),
      sumberAkuisisi:$("sumberAkuisisi").value, sumberDana:$("sumberDana").value,
      nominalPencairan:$("nominalPencairan").value.trim(), trxKembali:$("trxKembali").value.trim(),
      frekuensiKembali:$("frekuensiKembali").value.trim(), fileName:preparedPhoto.name,
      mimeType:preparedPhoto.type||"image/jpeg", fileBase64:base64
    };
    const response=await fetch(BACKEND,{method:"POST",headers:{"Content-Type":"text/plain;charset=utf-8"},body:JSON.stringify(payload)});
    const result=JSON.parse(await response.text());
    if(!result.success) throw new Error(result.message||"Backend menolak data.");
    const jobId=result.data.jobId;
    setStatus("✅ Data siap. Membuka BRIPORT Robot Android...","success");
    setTimeout(()=>openRobot(jobId),300);
  }catch(err){ console.error(err); setStatus("❌ GAGAL: "+(err.message||err),"error"); }
  finally{ btn.disabled=false; btn.textContent="MULAI PELAPORAN"; }
}

document.addEventListener("DOMContentLoaded",()=>{
  fillSelect("unitKerja",Uker);
  fillSelect("namaPPBK",PPBK);
  fillSelect("jenisPelaporan",JENIS);
  updateSummary();
});
