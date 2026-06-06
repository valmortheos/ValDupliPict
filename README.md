# ValDupliPict

**ValDupliPict** adalah aplikasi Android pintar yang dirancang untuk mendeteksi dan mengelola foto duplikat di perangkat Anda secara 100% offline. Aplikasi ini membantu Anda membebaskan ruang penyimpanan dengan mengidentifikasi gambar yang sama persis maupun yang memiliki kemiripan visual yang tinggi.

## 🌟 Fitur Utama

- **Deteksi Duplikat Identik:** Mencari gambar yang benar-benar sama menggunakan hash MD5, bahkan jika berada di folder yang berbeda.
- **Deteksi Kemiripan Visual (Pintar):**
  - Menggunakan algoritma **Perceptual Hashing (pHash)** dan **dHash** untuk menemukan gambar yang sudah di-resize, dikompres, atau sedikit diubah.
  - Verifikasi lanjutan menggunakan **Structural Similarity Index (SSIM)** dan **Perbandingan Histogram** untuk memastikan akurasi deteksi kemiripan yang tinggi (hingga 95%+).
- **Pemilihan Pintar (Smart Select):** Otomatis memilih gambar dengan kualitas terendah (ukuran lebih kecil, atau tanggal lebih lama) untuk dihapus, dan mempertahankan versi terbaik.
- **Recycle Bin Bawaan:** File yang dihapus dipindahkan ke folder `.valduplipict_trash` sebelum dihapus permanen, untuk mencegah penghapusan tidak disengaja.
- **Scan di Latar Belakang:** Proses pencarian duplikat dapat berjalan di background menggunakan `WorkManager`.

## 🛠️ Tech Stack & Arsitektur

- **Bahasa:** Kotlin
- **UI:** Jetpack Compose (Material Design 3)
- **Arsitektur:** MVVM + Clean Architecture
- **Database:** Room
- **Dependency Injection:** Dagger Hilt
- **Background Task:** WorkManager (CoroutineWorker)
- **Image Loading:** Coil
- **Build System:** Gradle (Kotlin DSL)

## 🚀 Setup & Build Otomatis (CI/CD)

Proyek ini menggunakan **GitHub Actions** untuk mem-build dan men-sign APK (Release) secara otomatis pada setiap `push` ke branch `main`.

### Generate Keystore (WAJIB menggunakan parameter ini)

```bash
keytool -genkeypair \
  -alias valduplipict-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -keystore keystore.jks \
  -storetype JKS \
  -storepass YOUR_STORE_PASSWORD \
  -keypass YOUR_KEY_PASSWORD \
  -dname "CN=ValDupliPict, OU=Dev, O=Valmortheosz, L=ID, ST=ID, C=ID"
```

> ⚠️ Gunakan Java 8 atau Java 11. Jangan gunakan Java 17+ untuk generate keystore.

### Encode Keystore ke Base64

```bash
# Linux / macOS
base64 -w 0 keystore.jks

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore.jks"))
```

### Isi GitHub Secrets

Buka repository → Settings → Secrets and variables → Actions → New repository secret

| Secret Name | Nilai |
|---|---|
| `KEYSTORE_BASE64` | Output base64 dari perintah di atas |
| `KEYSTORE_PASSWORD` | Password yang dipakai di `-storepass` |
| `KEY_ALIAS` | `valduplipict-key` |
| `KEY_PASSWORD` | Password yang dipakai di `-keypass` |

### Cara Mengunduh APK Release

Setelah GitHub Actions selesai berjalan:
1. Buka tab **Actions** di repositori GitHub.
2. Klik workflow run terbaru yang bernama **Build & Sign Release APK**.
3. Scroll ke bawah pada bagian **Artifacts**.
4. Unduh file zip `ValDupliPict-Release-APK`. Di dalamnya terdapat APK yang siap di-install di perangkat Anda!

## 🔐 Algoritma Deteksi Duplikat

Proses pencarian duplikat dilakukan melalui beberapa tahapan berjenjang (dari yang tercepat ke yang paling akurat):

1. **MD5 Hash Exact Match:** Mencari file identik 100%.
2. **File Size Bucketing (Pre-filter):** Mengabaikan file yang ukuran file-nya berbeda jauh (> 15%).
3. **Perceptual Hash (pHash):** Gambar di-resize dan dikonversi menggunakan proses sederhana (mirip Discrete Cosine Transform/DCT). Jarak Hamming (Hamming Distance) digunakan untuk mengukur kemiripan secara cepat.
4. **Structural Similarity Index (SSIM):** Untuk kandidat yang lolos pHash, SSIM akan menghitung kemiripan struktur, luminance, dan kontras pada blok 8x8 secara lokal (tanpa library OpenCV).
5. **Histogram Comparison:** Perbandingan distribusi warna RGB sebagai faktor keyakinan tambahan.

Kombinasi algoritma ini menghasilkan skor akhir kemiripan. Pengguna dapat mengatur *Threshold Kemiripan* (misalnya 90%) di menu Pengaturan.
