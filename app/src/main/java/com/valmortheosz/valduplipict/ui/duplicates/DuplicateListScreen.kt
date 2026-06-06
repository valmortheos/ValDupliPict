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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateListScreen(
    navController: NavController,
    viewModel: DuplicateListViewModel = hiltViewModel()
) {
    val duplicateGroups by viewModel.duplicateGroups.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val totalFiles = duplicateGroups.sumOf { it.files.size }
    val potentialSavings = duplicateGroups.sumOf { it.totalWastedSpace }

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header Summary
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Groups", style = MaterialTheme.typography.labelMedium)
                            Text("${duplicateGroups.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Files", style = MaterialTheme.typography.labelMedium)
                            Text("$totalFiles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Savings", style = MaterialTheme.typography.labelMedium)
                            Text(formatSize(potentialSavings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(duplicateGroups, key = { it.groupId }) { group ->
                        DuplicateGroupCard(
                            group = group,
                            selectedFiles = selectedFiles,
                            onFileSelect = { file -> viewModel.toggleSelection(file.filePath) },
                            onFileOpen = { file ->
                                val encodedPath = URLEncoder.encode(file.filePath, "UTF-8")
                                navController.navigate("photo_viewer/$encodedPath")
                            }
                        )
                    }
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
    onFileSelect: (ImageFile) -> Unit,
    onFileOpen: (ImageFile) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

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
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                group.files.forEach { file ->
                    val isSelected = selectedFiles.contains(file.filePath)
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onFileSelect(file) },
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Thumbnail
                        Box(
                            modifier = Modifier
                                .weight(0.3f)
                                .aspectRatio(1f)
                                .clickable { onFileOpen(file) }
                        ) {
                            AsyncImage(
                                model = File(file.filePath),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Details
                        Column(
                            modifier = Modifier.weight(0.7f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = file.fileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { onFileSelect(file) },
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = "Path: ${file.filePath.substringBeforeLast("/")}/",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Text(
                                text = "Size: ${formatSize(file.fileSize)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Resolution: ${file.width} x ${file.height}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Date: ${dateFormat.format(Date(file.lastModified * 1000))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (file != group.files.last()) {
                        HorizontalDivider()
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
