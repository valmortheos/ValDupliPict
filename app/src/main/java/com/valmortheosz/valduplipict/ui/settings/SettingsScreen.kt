package com.valmortheosz.valduplipict.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.valmortheosz.valduplipict.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val similarityThreshold by viewModel.similarityThreshold.collectAsState()
    val useTrash by viewModel.useTrash.collectAsState()
    val excludedFolders by viewModel.excludedFolders.collectAsState()

    var showAddFolderDialog by remember { mutableStateOf(false) }
    var newFolderPath by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_home)) },
                    selected = false,
                    onClick = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_results)) },
                    selected = false,
                    onClick = { navController.navigate("duplicates") { popUpTo("dashboard") } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                    selected = true,
                    onClick = { /* Already here */ }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // SCAN SETTINGS SECTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Scan Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.similarity_threshold),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = similarityThreshold,
                        onValueChange = { viewModel.updateThreshold(it) },
                        valueRange = 0.70f..1.0f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${(similarityThreshold * 100).toInt()}%",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            // EXCLUDED FOLDERS SECTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Excluded Folders", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                if (excludedFolders.isEmpty()) {
                    Text("No folders excluded.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    excludedFolders.forEach { folder ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(folder, modifier = Modifier.weight(1f), maxLines = 1)
                            IconButton(onClick = { viewModel.removeExcludedFolder(folder) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Folder", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showAddFolderDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Folder Exclusion")
                }
            }

            // STORAGE SECTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Storage", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.use_recycle_bin),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = useTrash,
                        onCheckedChange = { viewModel.updateUseTrash(it) }
                    )
                }
                Text(
                    text = stringResource(R.string.recycle_bin_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { navController.navigate("trash") }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.open_recycle_bin))
                }
            }

            // ABOUT SECTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("About", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(stringResource(R.string.app_version), style = MaterialTheme.typography.titleMedium)
                Text("Storage Engine: 1.0\nScan Engine: Local (Kotlin)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.app_desc))
            }
        }
    }

    if (showAddFolderDialog) {
        AlertDialog(
            onDismissRequest = { showAddFolderDialog = false },
            title = { Text("Add Folder to Exclude") },
            text = {
                OutlinedTextField(
                    value = newFolderPath,
                    onValueChange = { newFolderPath = it },
                    label = { Text("Folder Path") },
                    placeholder = { Text("e.g., /storage/emulated/0/WhatsApp") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newFolderPath.isNotBlank()) {
                        viewModel.addExcludedFolder(newFolderPath)
                    }
                    newFolderPath = ""
                    showAddFolderDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    newFolderPath = ""
                    showAddFolderDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
