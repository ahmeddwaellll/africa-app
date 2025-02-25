package com.example.africanschools.ui.screens.school.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.africanschools.data.model.SchoolDetailUiModel

@Composable
fun SchoolDetailScreen(
    schoolId: String,
    onNavigateUp: () -> Unit,
    viewModel: SchoolDetailViewModel = hiltViewModel()
) {
    val school by viewModel.school.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(schoolId) {
        viewModel.loadSchoolDetails(schoolId)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("School Details") }, navigationIcon = {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                school?.let {
                    SchoolDetailContent(school = it)
                } ?: run {
                    Text("School not found")
                }
            }
        }
    }
}

@Composable
fun SchoolDetailContent(school: SchoolDetailUiModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = school.name, style = MaterialTheme.typography.h5)
        // Additional school details can be added here
    }
}
