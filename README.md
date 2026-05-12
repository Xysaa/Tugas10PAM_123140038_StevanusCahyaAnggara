# 📝 Notes App — Week 10: Dependency Injection & Testing

**Nama:** Stevanus Cahya Anggara  
**NIM:** 123140038  
**Mata Kuliah:** Pengembangan Aplikasi Mobile — ITERA  
**Pertemuan:** 10 — Testing dan Dependency Injection

---

## 📋 Deskripsi Aplikasi

Aplikasi catatan (Notes App) berbasis **Kotlin Multiplatform (KMP)** dengan target Android, dibangun menggunakan:

- **Compose Multiplatform** — UI deklaratif lintas platform
- **SQLDelight** — Database lokal dengan query type-safe
- **Koin** — Dependency Injection ringan untuk KMP
- **kotlin.test + Turbine** — Unit test dan Flow test
- **Compose UI Test** — Instrumentasi UI test Android

---

## 🏗️ Arsitektur

Aplikasi mengikuti pola **MVVM (Model-View-ViewModel)** dengan **Clean Architecture** sederhana:

```
┌─────────────────────────────────────────┐
│              UI Layer                   │
│  NotesScreen  ←→  NotesViewModel        │
│  NoteDetailScreen ←→ NoteDetailViewModel│
└──────────────┬──────────────────────────┘
               │ (melalui interface)
┌──────────────▼──────────────────────────┐
│           Domain Layer                  │
│  NoteRepository (interface)             │
│  NoteValidator                          │
└──────────────┬──────────────────────────┘
               │ (Koin DI)
┌──────────────▼──────────────────────────┐
│            Data Layer                   │
│  NoteRepositoryImpl  ←  NoteDatabase    │
│  (SQLDelight)           (SQLDelight)    │
└─────────────────────────────────────────┘
```

---

## 📁 Struktur Project

```
composeApp/src/
├── androidMain/kotlin/com/example/notesapp/
│   ├── MainActivity.kt                    # Entry point + initKoin()
│   ├── data/local/
│   │   └── DatabaseDriverFactory.android.kt  # AndroidSqliteDriver
│   └── di/
│       └── AndroidAppModule.kt            # Koin module Android-specific
│
├── commonMain/kotlin/com/example/notesapp/
│   ├── App.kt                             # Navigation + MaterialTheme
│   ├── data/
│   │   ├── local/
│   │   │   └── DatabaseDriverFactory.kt   # expect class (KMP)
│   │   ├── model/
│   │   │   ├── Note.kt                    # Data class catatan
│   │   │   └── Category.kt               # Data class kategori
│   │   ├── repository/
│   │   │   ├── NoteRepository.kt          # Interface
│   │   │   └── NoteRepositoryImpl.kt      # Implementasi SQLDelight
│   │   └── validation/
│   │       └── NoteValidator.kt           # Validasi input
│   ├── di/
│   │   ├── AppModule.kt                   # dataModule + viewModelModule
│   │   └── KoinInitializer.kt             # fun initKoin()
│   ├── ui/
│   │   ├── notes/
│   │   │   ├── NotesScreen.kt             # Layar daftar catatan
│   │   │   ├── NotesViewModel.kt          # ViewModel daftar
│   │   │   └── NotesUiState.kt            # Sealed class UI state
│   │   ├── detail/
│   │   │   ├── NoteDetailScreen.kt        # Layar tambah/edit
│   │   │   └── NoteDetailViewModel.kt     # ViewModel detail
│   │   └── components/
│   │       ├── NoteCard.kt               # Kartu catatan
│   │       ├── SearchBar.kt              # Bar pencarian
│   │       └── CategoryChip.kt           # Chip filter kategori
│   └── util/
│       └── TestTags.kt                    # Konstanta test tags
│
├── commonMain/sqldelight/com/example/notesapp/data/local/
│   └── Note.sq                            # Schema + 8 SQL queries
│
├── iosMain/kotlin/com/example/notesapp/
│   └── data/local/
│       └── DatabaseDriverFactory.ios.kt   # NativeSqliteDriver
│
├── commonTest/kotlin/com/example/notesapp/
│   ├── repository/
│   │   └── NoteRepositoryTest.kt          # 7 unit test cases
│   ├── viewmodel/
│   │   └── NotesViewModelTest.kt          # 5 unit test cases
│   └── flow/
│       └── NoteFlowTest.kt               # 4 Flow test cases (Turbine)
│
└── androidInstrumentedTest/kotlin/com/example/notesapp/
    └── ui/
        └── NotesScreenTest.kt             # 5 UI test cases
```

---

## 🔧 Dependency Injection (Koin)

Koin dikonfigurasi dengan **2 modul utama**:

```kotlin
// dataModule — layer data
val dataModule = module {
    single { NoteDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single<NoteRepository> { NoteRepositoryImpl(get()) }
    factory { NoteValidator() }
}

// viewModelModule — layer ViewModel
val viewModelModule = module {
    viewModel { NotesViewModel(get(), get()) }
    viewModel { NoteDetailViewModel(get(), get()) }
}
```

`initKoin()` dipanggil di `MainActivity.onCreate()` dengan menyuntikkan Android Context melalui `androidModule`.

---

## 🧪 Daftar Test Cases

### Unit Test: NoteRepository (7 test cases)

| ID | Deskripsi | Status |
|---|---|---|
| TC-01 | `insertNote` berhasil menyimpan catatan baru | ✅ |
| TC-02 | `getAllNotes` mengembalikan semua catatan sebagai Flow | ✅ |
| TC-03 | `deleteNote` menghapus catatan berdasarkan id dengan benar | ✅ |
| TC-04 | `updateNote` mengubah data catatan yang sudah ada | ✅ |
| TC-05 | `searchNotes` mengembalikan catatan sesuai kata kunci | ✅ |
| TC-06 | `getAllNotes` mengembalikan list kosong saat database kosong | ✅ |
| TC-07 | `getNotesByKategori` hanya mengembalikan kategori yang sesuai | ✅ |

### Unit Test: NotesViewModel — MockK/Spy (5 test cases)

| ID | Deskripsi | Status |
|---|---|---|
| TC-01 | State awal Loading kemudian berubah menjadi Success | ✅ |
| TC-02 | `tambahNote` memanggil `repository.insertNote` | ✅ |
| TC-03 | `hapusNote` memanggil `repository.deleteNote` dengan id yang benar | ✅ |
| TC-04 | `cariNote` memfilter daftar catatan berdasarkan query | ✅ |
| TC-05 | `tambahNote` dengan judul kosong menghasilkan state Error | ✅ |

### Flow Test — Turbine (4 test cases)

| ID | Deskripsi | Status |
|---|---|---|
| TC-01 | Flow `getAllNotes` emit list kosong saat database kosong | ✅ |
| TC-02 | Flow `getAllNotes` emit data terbaru setelah catatan diinsert | ✅ |
| TC-03 | Flow emit perubahan secara berurutan setelah insert dan delete | ✅ |
| TC-04 | Flow `searchNotes` emit hanya catatan yang sesuai query | ✅ |

### UI Test — Compose Test (5 test cases)

| ID | Deskripsi | Status |
|---|---|---|
| TC-01 | State kosong menampilkan teks "Belum ada catatan" | ✅ |
| TC-02 | Daftar catatan menampilkan item yang sudah tersimpan | ✅ |
| TC-03 | Hapus catatan menghilangkan item dari daftar | ✅ |
| TC-04 | Search bar menampilkan hanya catatan sesuai kata kunci | ✅ |
| TC-05 | Tombol tambah FAB dapat diklik dan memicu navigasi | ✅ |

**Total: 21 test cases**

---

## 📦 Dependencies

| Library | Versi | Kegunaan |
|---|---|---|
| Kotlin | 2.3.21 | Bahasa pemrograman |
| Compose Multiplatform | 1.10.3 | UI framework |
| Koin | 4.0.2 | Dependency Injection |
| SQLDelight | 2.0.2 | Database lokal |
| MockK | 1.14.0 | Mocking untuk Android test |
| Turbine | 1.2.1 | Flow testing |
| kotlinx-coroutines-test | 1.10.2 | Coroutines test utilities |
| AGP | 8.11.2 | Android Gradle Plugin |

---

## 🚀 Cara Menjalankan

### Menjalankan Unit Test
```bash
./gradlew :composeApp:allTests
```

### Menjalankan Unit Test saja (commonTest)
```bash
./gradlew :composeApp:testDebugUnitTest
```

### Menjalankan UI Test (perlu emulator/device)
```bash
./gradlew :composeApp:connectedAndroidTest
```

### Build APK Debug
```bash
./gradlew :composeApp:assembleDebug
```

---

## ✅ Checklist Implementasi

- [x] Koin DI terkonfigurasi dengan `dataModule` dan `viewModelModule`
- [x] `initKoin()` dipanggil saat app start di `MainActivity.onCreate()`
- [x] Interface `NoteRepository` digunakan (bukan langsung impl)
- [x] Constructor injection digunakan (bukan service locator)
- [x] SQLDelight database dengan 8 queries di `Note.sq`
- [x] CRUD lengkap: Create, Read, Update, Delete
- [x] Search/filter catatan berdasarkan kata kunci
- [x] Filter catatan berdasarkan kategori
- [x] `NoteValidator` memvalidasi input sebelum simpan
- [x] `TestTags` dipasang di semua elemen UI yang ditest
- [x] Pola AAA (Arrange-Act-Assert) di semua unit test
- [x] Komentar kode dalam Bahasa Indonesia
- [x] 21 test cases total (melebihi minimum)

---

# 📸 Screenshot

![HasilCoverage](https://github.com/user-attachments/assets/726fcff3-0016-4eb6-a1c5-db611449d84a)
![HasilTest](https://github.com/user-attachments/assets/8de4e836-5cf4-42b9-8c6c-d83febb60ff0)

---

## 🎥 Video Demo
[DEMO](https://drive.google.com/file/d/1ctIkyPunEKEEexT2ImhjaGeTieQp4PcJ/view?usp=sharing)
