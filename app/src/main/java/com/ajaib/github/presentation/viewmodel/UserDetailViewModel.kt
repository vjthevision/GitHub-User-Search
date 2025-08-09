package com.ajaib.github.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajaib.github.domain.model.User
import com.ajaib.github.domain.usecase.GetUserDetailsUseCase
import com.ajaib.github.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val getUserDetailsUseCase: GetUserDetailsUseCase
) : ViewModel() {

    private val _username = MutableStateFlow("")

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val userDetails: StateFlow<Resource<User>> = _username
        .filter { it.isNotEmpty() }
        .flatMapLatest { username ->
            getUserDetailsUseCase(username)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading()
        )

    fun loadUserDetails(username: String) {
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
