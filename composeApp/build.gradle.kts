plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    // Plugin SQLDelight untuk men-generate kode Kotlin dari file .sq
    alias(libs.plugins.sqldelight)
    // Kover untuk laporan test coverage
    alias(libs.plugins.kover)
}

kotlin {
    // Gunakan jvmToolchain sesuai JDK yang terinstall di mesin (JDK 17)
    // Menggantikan compilerOptions DSL yang tidak kompatibel dengan AGP 8.7+
    jvmToolchain(17)

    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        // ----------------------------------------------------------------
        // Dependensi untuk semua platform (Android & iOS)
        // ----------------------------------------------------------------
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            // Koin untuk Android
            implementation(libs.koin.android)
            // SQLDelight driver untuk Android (menggunakan SQLite bawaan Android)
            implementation(libs.sqldelight.android.driver)
        }

        // ----------------------------------------------------------------
        // Dependensi bersama (common) untuk semua platform
        // ----------------------------------------------------------------
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Koin core untuk Dependency Injection
            implementation(libs.koin.core)
            // Koin Compose integration
            implementation(libs.koin.compose)
            // Koin Compose ViewModel untuk koinViewModel() di Composable
            implementation(libs.koin.compose.viewmodel)
            // SQLDelight coroutines extensions untuk Flow support
            implementation(libs.sqldelight.coroutines.extensions)
            // Catatan: waktu multiplatform ditangani via expect/actual di masing-masing platform
            // Android: System.currentTimeMillis() | iOS: NSDate.timeIntervalSince1970
        }

        // ----------------------------------------------------------------
        // Dependensi untuk iOS (menggunakan SQLite native driver)
        // ----------------------------------------------------------------
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }

        // ----------------------------------------------------------------
        // Dependensi untuk unit test bersama (common test)
        // ----------------------------------------------------------------
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            // Coroutines test untuk pengujian suspend function dan Flow
            implementation(libs.kotlinx.coroutines.test)
            // Turbine untuk pengujian Flow secara mudah
            implementation(libs.turbine)
            // Koin test utilities
            implementation(libs.koin.test)
        }
    }
}

// ----------------------------------------------------------------
// Konfigurasi SQLDelight: mendefinisikan nama database dan package
// ----------------------------------------------------------------
sqldelight {
    databases {
        create("NoteDatabase") {
            // Package yang akan digunakan untuk kode yang di-generate SQLDelight
            packageName.set("com.example.notesapp.data.local")
        }
    }
}

android {
    namespace = "com.example.notesapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.notesapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        // Dibutuhkan untuk Android Instrumented Test (Compose UI Test)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)

    // ----------------------------------------------------------------
    // MockK — hanya untuk JVM/Android test (BUKAN commonTest)
    // ----------------------------------------------------------------
    // MockK untuk unit test di JVM
    testImplementation(libs.mockk)
    // MockK untuk Android Instrumented Test
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)

    // ----------------------------------------------------------------
    // Compose UI Test untuk Android Instrumented Test
    // ----------------------------------------------------------------
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}


// ----------------------------------------------------------------
// Konfigurasi Kover untuk laporan test coverage
// ----------------------------------------------------------------
kover {
    reports {
        filters {
            excludes {
                // Kecualikan kelas yang di-generate (SQLDelight, BuildConfig, dll)
                classes(
                    "*.BuildConfig",
                    "*.*\$\$serializer",
                    "com.example.notesapp.data.local.*",  // kelas generated SQLDelight
                )
                // Kecualikan file UI (Compose) dari perhitungan coverage
                packages("com.example.notesapp.ui.*")
            }
        }
        total {
            html {
                // Output HTML report ke folder yang mudah ditemukan
                onCheck = true
            }
        }
    }
}
