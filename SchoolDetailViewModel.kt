import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SchoolDetailViewModel @Inject constructor(
    private val repository: SchoolRepository
) : ViewModel() {
    
    // Define states for loading, success, and error
    sealed class State {
        object Loading : State()
        data class Success(val school: SchoolDetail) : State()
        data class Error(val message: String) : State()
    }
    
    var state by mutableStateOf<State>(State.Loading)
        private set

    fun loadSchoolDetails(schoolId: String) {
        viewModelScope.launch {
            state = State.Loading
            try {
                val school = repository.getSchoolDetails(schoolId)
                state = State.Success(school)
            } catch (e: Exception) {
                state = State.Error("Failed to load school details")
            }
        }
    }
}
