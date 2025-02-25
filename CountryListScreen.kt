package com.example.africanschools.ui.screens.country

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.africanschools.data.model.CountryUiModel
import com.example.africanschools.ui.base.BaseViewModel

@Composable
fun CountryListScreen(
    onCountryClick: (String) -> Unit,
    viewModel: CountryListViewModel = hiltViewModel()
) {
    val countries by viewModel.countries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Countries") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn {
                    items(countries) { country ->
                        CountryListItem(country = country, onClick = { onCountryClick(country.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun CountryListItem(country: CountryUiModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = country.name, style = MaterialTheme.typography.h6)
            // Additional country details can be added here
        }
    }
}
