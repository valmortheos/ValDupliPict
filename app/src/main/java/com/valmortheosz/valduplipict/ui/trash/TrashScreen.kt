package com.valmortheosz.valduplipict.ui.trash

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.valmortheosz.valduplipict.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    navController: NavController,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val trashFiles by viewModel.trashFiles.collectAsState()
    var showEmptyBinConfirmation by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        if (trashFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.bin_empty_msg))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(trashFiles) { file ->
                    Card(
                        modifier = Modifier.aspectRatio(1f)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = java.io.File(file.trashPath),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    FilledIconButton(
                                        onClick = { viewModel.restoreFile(file) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Restore, contentDescription = "Restore", modifier = Modifier.size(16.dp))
                                    }
                                    FilledIconButton(
                                        onClick = { viewModel.deleteFilePermanently(file) },
                                        modifier = Modifier.size(32.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEmptyBinConfirmation) {
        AlertDialog(
            onDismissRequest = { showEmptyBinConfirmation = false },
            title = { Text("Empty Recycle Bin?") },
            text = { Text("This will permanently delete all files in the recycle bin. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.emptyTrash()
                        showEmptyBinConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEmptyBinConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
