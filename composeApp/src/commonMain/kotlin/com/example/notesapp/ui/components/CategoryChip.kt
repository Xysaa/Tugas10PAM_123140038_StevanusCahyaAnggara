package com.example.notesapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.notesapp.util.TestTags

/**
 * Komponen chip untuk menampilkan dan memilih kategori catatan.
 *
 * Chip yang dipilih akan ditampilkan dengan warna berbeda (primary).
 *
 * @param nama      Nama kategori yang ditampilkan di chip
 * @param dipilih   Apakah chip ini sedang dipilih/aktif
 * @param onClick   Dipanggil saat chip ditekan
 * @param modifier  Modifier tambahan dari pemanggil
 */
@Composable
fun CategoryChip(
    nama: String,
    dipilih: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedFilterChip(
        selected = dipilih,
        onClick = onClick,
        label = {
            Text(
                text = nama,
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = FilterChipDefaults.elevatedFilterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
            .padding(end = 8.dp)
            // Tag unik per kategori untuk keperluan UI test
            .testTag("${TestTags.CHIP_KATEGORI}_$nama")
    )
}
