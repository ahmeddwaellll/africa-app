package com.example.africanschools.ui.screens.school

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.africanschools.data.model.SchoolUiModel

@Composable
fun SchoolListScreen(
    provinceId: String,
    onSchoolClick: (String) -> Unit,
    viewModel: SchoolListViewModel = hiltViewModel()
) {
    val schools by viewModel.schools.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Schools") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn {
                    items(schools) { school ->
                        SchoolListItem(school = school, onClick = { onSchoolClick(school.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun SchoolListItem(school: SchoolUiModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = school.name, style = MaterialTheme.typography.h6)
            // Additional school details can be added here
        }
    }
}
