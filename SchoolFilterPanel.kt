package com.example.africanschools.ui.screens.school

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.Text
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.RangeSlider
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SchoolFilterPanel(
    minAcceptanceRate: Int,
    maxAcceptanceRate: Int,
    onRangeChange: (Int, Int) -> Unit,
    onFeesChange: (Double, Double) -> Unit,
    onTypeChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val schoolTypes = listOf("Public", "Private", "International")
    var selectedType by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Filter Schools", fontWeight = FontWeight.Bold)        
        // Additional filters for fees and school type
        Text("Fees Range", fontWeight = FontWeight.Bold)
        RangeSlider(
            value = minFees.toFloat()..maxFees.toFloat(),
            valueRange = 0f..100000f,
            onValueChange = { range -> 
                onFeesChange(range.start.toDouble(), range.endInclusive.toDouble())
            }
        )
        
        Text("School Type", fontWeight = FontWeight.Bold)
        Text(selectedType.ifEmpty { "Select Type" }, modifier = Modifier.clickable { expanded = true })
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            schoolTypes.forEach { type ->
                DropdownMenuItem(onClick = {
                    onTypeChange(type)
                    selectedType = type
                    expanded = false
                }) {
                    Text(text = type)
                }
            }
        }

        RangeSlider(
            value = minAcceptanceRate.toFloat()..maxAcceptanceRate.toFloat(),
            valueRange = 0f..100f,
            onValueChange = { range ->
                onRangeChange(range.start.toInt(), range.endInclusive.toInt())
            }
        )
    }
}
