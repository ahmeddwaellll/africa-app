package com.example.africanschools.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.graphics.Color

@Composable
fun AIAssistantScreen(
    viewModel: AIAssistantViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPremiumDialog by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "AI-Powered Application Assistant",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Button(
                onClick = { showPremiumDialog = true },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.secondary
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Upgrade to Premium")
            }
        }
        
        if (showPremiumDialog) {
            AlertDialog(
                onDismissRequest = { showPremiumDialog = false },
                title = { Text("Premium Features") },
                text = { 
                    Text("Unlock automatic school applications and advanced AI assistance for just $9.99/month")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Implement payment processing
                            showPremiumDialog = false
                        }
                    ) {
                        Text("Subscribe")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPremiumDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Error Message
        AnimatedVisibility(
            visible = uiState.error != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            uiState.error?.let { error ->
                Card(
                    backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        
        // Chat History
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(uiState.messages) { message ->
                ChatBubble(message = message)
            }
        }
        
        // Loading Indicator
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colors.primary
            )
        }
        
        // Input Section
        Card(
            elevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = uiState.userInput,
                    onValueChange = viewModel::onInputChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your question...") },
                    enabled = !uiState.isLoading,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent
                    )
                )
                
                IconButton(
                    onClick = viewModel::onSendClicked,
                    enabled = uiState.userInput.isNotBlank() && !uiState.isLoading
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (uiState.userInput.isNotBlank() && !uiState.isLoading)
                            MaterialTheme.colors.primary
                        else
                            MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
