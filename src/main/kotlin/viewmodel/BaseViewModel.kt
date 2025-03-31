package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Base ViewModel class that provides common functionality for all ViewModels
 */
abstract class BaseViewModel {
    // Coroutine scope for viewmodel operations
    protected val viewModelScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate + CoroutineExceptionHandler { _, throwable ->
            handleError(throwable)
        }
    )

    // Error handling
    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errors: SharedFlow<String> = _errors

    // Loading state
    var isLoading by mutableStateOf(false)
        protected set

    /**
     * Handle errors that occur during coroutine execution
     */
    protected fun handleError(throwable: Throwable) {
        logger.error(throwable) { "Error in ViewModel: ${this::class.simpleName}" }
        viewModelScope.launch {
            _errors.emit(throwable.message ?: "An unknown error occurred")
        }
    }

    /**
     * Launch a coroutine with loading state
     */
    protected fun launchWithLoading(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch {
            try {
                isLoading = true
                block()
            } catch (e: Exception) {
                handleError(e)
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Clean up resources when the ViewModel is no longer needed
     */
    open fun clear() {
        viewModelScope.cancel()
    }
}
