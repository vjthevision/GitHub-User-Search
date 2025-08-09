package com.ajaib.github.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajaib.github.domain.model.Repository
import com.ajaib.github.domain.usecase.GetUserRepositoriesUseCase
import com.ajaib.github.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserRepositoriesViewModel @Inject constructor(
    private val getUserRepositoriesUseCase: GetUserRepositoriesUseCase
) : ViewModel() {

    private val _username = MutableStateFlow("")

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val repositories: StateFlow<Resource<List<Repository>>> = _username
        .filter { it.isNotEmpty() }
        .flatMapLatest { username ->
            getUserRepositoriesUseCase(username)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading()
        )

    fun loadRepositories(username: String) {
        _username.value = username
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Re-trigger the flow by emitting the current username
            val currentUsername = _username.value
            _username.value = ""
            _username.value = currentUsername
            _isRefreshing.value = false
        }
    }
}