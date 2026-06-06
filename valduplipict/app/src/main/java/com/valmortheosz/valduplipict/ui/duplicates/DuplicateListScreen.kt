package com.valmortheosz.valduplipict.ui.duplicates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.valmortheosz.valduplipict.data.model.DuplicateGroup
import com.valmortheosz.valduplipict.data.model.ImageFile
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateListScreen(
    navController: NavController,
    viewModel: DuplicateListViewModel = hiltViewModel()
) {
    val duplicateGroups by viewModel.duplicateGroups.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_results_title)) },
                actions = {
                    IconButton(onClick = { viewModel.autoSelectSmart() }) {
                        Text("✨") // Smart Select
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Text("🏠") },
                    label = { Text(stringResource(R.string.nav_home)) },
                    selected = false,
                    onClick = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } }
                )
                NavigationBarItem(
                    icon = { Text("🔍") },
                    label = { Text(stringResource(R.string.nav_results)) },
                    selected = true,
                    onClick = { /* Already here */ }
                )
                NavigationBarItem(
                    icon = { Text("⚙️") },
                    label = { Text(stringResource(R.string.nav_settings)) },
                    selected = false,
                    onClick = { navController.navigate("settings") }
                )
            }
        },
        floatingActionButton = {
            if (selectedFiles.isNotEmpty()) {
                FloatingActionButton(onClick = { showDeleteConfirmation = true }) {
                    Text("🗑️") // Delete icon
                }
            }
        }
    ) { paddingValues ->
        if (duplicateGroups.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_duplicates))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(duplicateGroups, key = { it.groupId }) { group ->
                    DuplicateGroupCard(
                        group = group,
                        selectedFiles = selectedFiles,
                        onFileClick = { file -> viewModel.toggleSelection(file.filePath) }
                    )
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteConfirmation = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.confirm_delete_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.confirm_delete_desc, selectedFiles.size),
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                    Button(
                        onClick = {
                            viewModel.deleteSelectedFiles()
                            showDeleteConfirmation = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.btn_delete))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DuplicateGroupCard(
    group: DuplicateGroup,
    selectedFiles: Set<String>,
    onFileClick: (ImageFile) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.similar_pct, (group.similarityScore * 100).toInt()),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (group.similarityScore >= 0.95f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(R.string.saved_amount, formatSize(group.totalWastedSpace)),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.files.forEach { file ->
                    val isSelected = selectedFiles.contains(file.filePath)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { onFileClick(file) }
                    ) {
                        AsyncImage(
                            model = File(file.filePath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isSelected) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("✅", style = MaterialTheme.typography.headlineMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatSize(sizeInBytes: Long): String {
    if (sizeInBytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.getDefault(), "%.2f %s", sizeInBytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
