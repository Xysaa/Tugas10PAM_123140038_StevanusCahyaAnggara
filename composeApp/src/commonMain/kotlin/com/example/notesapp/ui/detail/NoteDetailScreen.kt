package com.example.notesapp.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.notesapp.data.model.Category
import com.example.notesapp.data.model.Note
import com.example.notesapp.data.model.currentTimeMs
import com.example.notesapp.util.TestTags
import org.koin.compose.viewmodel.koinViewModel

/**
 * Layar form untuk menambah catatan baru atau mengedit catatan yang ada.
 *
 * Jika [noteId] null maka mode tambah baru; jika berisi id maka mode edit.
 *
 * @param noteId           Id catatan yang akan diedit (null = tambah baru)
 * @param onNavigateBack   Dipanggil saat pengguna selesai atau menekan tombol kembali
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: NoteDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // State form input
    var judul    by remember { mutableStateOf("") }
    var isi      by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("Umum") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Muat data catatan jika mode edit
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.muatNote(noteId)
        }
    }

    // Isi form saat data catatan berhasil dimuat (mode edit)
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is NoteDetailUiState.Ready -> {
                judul    = state.note.judul
                isi      = state.note.isi
                kategori = state.note.kategori
            }
            is NoteDetailUiState.SaveSuccess -> {
                // Kembali ke layar sebelumnya setelah simpan berhasil
                onNavigateBack()
            }
            is NoteDetailUiState.Error -> {
                // Tampilkan pesan error di Snackbar
                snackbarHostState.showSnackbar(state.pesan)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (noteId == null) "Catatan Baru" else "Edit Catatan",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Input judul catatan
            OutlinedTextField(
                value = judul,
                onValueChange = { judul = it },
                label = { Text("Judul") },
                placeholder = { Text("Masukkan judul catatan") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.INPUT_JUDUL)
            )

            // Input isi catatan (multi-line)
            OutlinedTextField(
                value = isi,
                onValueChange = { isi = it },
                label = { Text("Isi Catatan") },
                placeholder = { Text("Tulis isi catatan di sini...") },
                minLines = 5,
                maxLines = 15,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.INPUT_ISI)
            )

            // Dropdown selector kategori
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.INPUT_KATEGORI)
            ) {
                OutlinedTextField(
                    value = kategori,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    // Tampilkan semua kategori default sebagai pilihan
                    Category.DEFAULT_CATEGORIES.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.nama) },
                            onClick = {
                                kategori = cat.nama
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol simpan
            Button(
                onClick = {
                    if (noteId == null) {
                        // Mode tambah baru
                        viewModel.simpanNoteBaru(judul, isi, kategori)
                    } else {
                        // Mode edit: perlu mengetahui note asli untuk mempertahankan tanggalDibuat
                        val noteAsli = (uiState as? NoteDetailUiState.Ready)?.note
                        viewModel.perbaruiNote(
                            Note(
                                id            = noteId,
                                judul         = judul,
                                isi           = isi,
                                kategori      = kategori,
                                tanggalDibuat = noteAsli?.tanggalDibuat ?: currentTimeMs(),
                                tanggalDiubah = currentTimeMs()
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.TOMBOL_SIMPAN)
            ) {
                Text(
                    text = "Simpan Catatan",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
