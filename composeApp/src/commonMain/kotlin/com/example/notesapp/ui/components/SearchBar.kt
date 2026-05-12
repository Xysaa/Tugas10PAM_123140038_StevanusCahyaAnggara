package com.example.notesapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.notesapp.util.TestTags

/**
 * Komponen search bar untuk mencari catatan berdasarkan kata kunci.
 *
 * @param query       Teks pencarian yang sedang aktif
 * @param onQueryChange Dipanggil setiap kali teks berubah
 * @param modifier    Modifier tambahan dari pemanggil
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Cari catatan...",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag(TestTags.SEARCH_BAR)
    )
}
