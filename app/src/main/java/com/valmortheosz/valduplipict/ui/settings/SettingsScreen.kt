package com.valmortheosz.valduplipict.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
                    icon = { Text("🏠") },
                    label = { Text(stringResource(R.string.nav_home)) },
                    selected = false,
                    onClick = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } }
                )
                NavigationBarItem(
                    icon = { Text("🔍") },
                    label = { Text(stringResource(R.string.nav_results)) },
                    selected = false,
                    onClick = { navController.navigate("duplicates") { popUpTo("dashboard") } }
                )
                NavigationBarItem(
                    icon = { Text("⚙️") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("trash") }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.open_recycle_bin))
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.about_app),
                style = MaterialTheme.typography.titleLarge
            )
            Text(stringResource(R.string.app_version))
            Text(stringResource(R.string.app_desc))
        }
    }
}
