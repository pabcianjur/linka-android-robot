const FORM_ID = '1mSgM-Smettzn8UwVRG3Q3IF-KXTwhzMhtCmwBpPrYfg';
const UPLOAD_FOLDER_NAME = 'BRILink BRIPORT Final Uploads';
const JOB_PREFIX = 'BRIPORT_JOB_';

function doGet(e) {
  try {
    const p = e && e.parameter ? e.parameter : {};
    const action = p.action || 'health';
    if (action === 'health') return json_({success:true, service:'BRIPORT Final Backend', mode:'FINAL'});
    if (action === 'job') return getJob_(p.jobId);
    if (action === 'file') return getFile_(p.jobId);
    return json_({success:false,message:'Action tidak dikenal.'});
  } catch (err) {
    return json_({success:false,message:String(err && err.message ? err.message : err)});
  }
}

function doPost(e) {
  try {
    const p = JSON.parse(e.postData.contents || '{}');
    if (p.mode !== 'FINAL') throw new Error('Backend FINAL menerima mode FINAL saja.');
    const required = ['branchOffice','unitKerja','role','namaPPBK','jenisPelaporan','idOutlet','namaAgen','fileName','mimeType','fileBase64'];
    required.forEach(k => { if (p[k] === undefined || p[k] === null || String(p[k]).trim() === '') throw new Error('Data belum lengkap: ' + k); });

    const bytes = Utilities.base64Decode(p.fileBase64);
    const blob = Utilities.newBlob(bytes, p.mimeType, p.fileName);
    const file = getUploadFolder_().createFile(blob);
    const prefilledUrl = buildPrefilledUrl_(p);
    const jobId = Utilities.getUuid();

    const job = {
      jobId,
      createdAt: new Date().toISOString(),
      branchOffice:p.branchOffice, unitKerja:p.unitKerja, role:p.role, namaPPBK:p.namaPPBK,
      jenisPelaporan:p.jenisPelaporan, idOutlet:p.idOutlet, namaAgen:p.namaAgen,
      sumberAkuisisi:p.sumberAkuisisi || '', sumberDana:p.sumberDana || '',
      nominalPencairan:p.nominalPencairan || '', trxKembali:p.trxKembali || '', frekuensiKembali:p.frekuensiKembali || '',
      fileId:file.getId(), fileName:file.getName(), mimeType:file.getMimeType(), fileSize:file.getSize(),
      prefilledUrl
    };
    PropertiesService.getScriptProperties().setProperty(JOB_PREFIX + jobId, JSON.stringify(job));

    return json_({success:true,mode:'FINAL',message:'Job BRIPORT siap.',data:job});
  } catch(err) {
    return json_({success:false,mode:'FINAL',message:String(err && err.message ? err.message : err)});
  }
}

function buildPrefilledUrl_(p) {
  const form = FormApp.openById(FORM_ID);
  const response = form.createResponse();
  const values = {
    'LIST Branch Office': p.branchOffice,
    'LIST Unit Kerja Cianjur': p.unitKerja,
    'LIST Pelaporan oleh': p.role,
    'LIST Nama PPBK': p.namaPPBK,
    'LIST Jenis Pelaporan': p.jenisPelaporan,
    'ID Outlet': p.idOutlet,
    'Nama Agen': p.namaAgen,
    'Sumber Akuisisi / Tambah Alat': p.sumberAkuisisi,
    'Sumber Dana': p.sumberDana,
    'Nominal Pencairan Dantal (Khusus Dana Talangan Bank Raya)': p.nominalPencairan,
    'Total Nominal yang sudah di Trx Kembali ke BRILink': p.trxKembali,
    'Berapa kali di transaksikan Kembali ke BRILink?': p.frekuensiKembali
  };
  form.getItems().forEach(item => {
    const title = item.getTitle();
    const value = values[title];
    if (value === undefined || value === null || String(value).trim() === '') return;
    try {
      const type = item.getType();
      if (type === FormApp.ItemType.LIST) response.withItemResponse(item.asListItem().createResponse(String(value)));
      else if (type === FormApp.ItemType.MULTIPLE_CHOICE) response.withItemResponse(item.asMultipleChoiceItem().createResponse(String(value)));
      else if (type === FormApp.ItemType.TEXT) response.withItemResponse(item.asTextItem().createResponse(String(value)));
      else if (type === FormApp.ItemType.PARAGRAPH_TEXT) response.withItemResponse(item.asParagraphTextItem().createResponse(String(value)));
    } catch (err) {
      console.log('Prefill skip: ' + title + ' => ' + err);
    }
  });
  return response.toPrefilledUrl();
}

function getJob_(jobId) {
  if (!jobId) throw new Error('jobId wajib diisi.');
  const raw = PropertiesService.getScriptProperties().getProperty(JOB_PREFIX + jobId);
  if (!raw) throw new Error('Job tidak ditemukan.');
  const job = JSON.parse(raw);
  return json_({success:true,data:job});
}

function getFile_(jobId) {
  if (!jobId) throw new Error('jobId wajib diisi.');
  const raw = PropertiesService.getScriptProperties().getProperty(JOB_PREFIX + jobId);
  if (!raw) throw new Error('Job tidak ditemukan.');
  const job = JSON.parse(raw);
  const file = DriveApp.getFileById(job.fileId);
  const blob = file.getBlob();
  return json_({success:true,fileName:file.getName(),mimeType:blob.getContentType(),base64:Utilities.base64Encode(blob.getBytes())});
}

function getUploadFolder_() {
  const it = DriveApp.getFoldersByName(UPLOAD_FOLDER_NAME);
  return it.hasNext() ? it.next() : DriveApp.createFolder(UPLOAD_FOLDER_NAME);
}

function json_(obj) {
  return ContentService.createTextOutput(JSON.stringify(obj)).setMimeType(ContentService.MimeType.JSON);
}
