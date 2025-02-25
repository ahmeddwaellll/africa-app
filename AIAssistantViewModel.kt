package com.example.africanschools.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.africanschools.data.network.DeepSeekApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIAssistantViewModel @Inject constructor(
    private val apiService: DeepSeekApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIAssistantUiState())
    val uiState: StateFlow<AIAssistantUiState> = _uiState.asStateFlow()

    var userInput by mutableStateOf("")
        private set

    fun onInputChanged(input: String) {
        userInput = input
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun onSendClicked() {
        if (userInput.isBlank()) return
        
        val message = Message(text = userInput, isUser = true)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + message,
            userInput = "",
            isLoading = true,
            error = null
        )
        viewModelScope.launch {
            try {
                val response = apiService.generateResponse(userInput).await()
                val aiMessage = Message(text = response, isUser = false)
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to get response: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
}

data class AIAssistantUiState(
    val messages: List<Message> = emptyList(),
    val userInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class Message(
    val text: String,
    val isUser: Boolean
)
