package com.example.notesapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.notesapp.data.model.Note
import com.example.notesapp.util.TestTags
import org.jetbrains.compose.resources.painterResource

/**
 * Komponen kartu catatan yang ditampilkan di daftar.
 *
 * Menampilkan judul, isi (dipotong), kategori, dan tombol hapus.
 *
 * @param note       Data catatan yang ditampilkan
 * @param onClick    Dipanggil saat kartu ditekan (untuk membuka detail/edit)
 * @param onHapus    Dipanggil saat tombol hapus ditekan
 * @param modifier   Modifier tambahan dari pemanggil
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onHapus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            // Setiap kartu diberi tag unik menggunakan id catatan
            .testTag("${TestTags.ITEM_CATATAN}_${note.id}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Judul catatan
                Text(
                    text = note.judul,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Tombol hapus catatan
                IconButton(
                    onClick = onHapus,
                    modifier = Modifier.testTag("${TestTags.TOMBOL_HAPUS}_${note.id}")
                ) {
                    Text(
                        text = "✕",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Isi catatan (dibatasi 2 baris)
            Text(
                text = note.isi,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Chip kategori kecil di bagian bawah kartu
            CategoryChip(
                nama = note.kategori,
                dipilih = false,
                onClick = {}
            )
        }
    }
}
