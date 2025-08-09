package com.ajaib.github.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajaib.github.data.remote.dto.UserDto
import com.ajaib.github.data.repository.UserRepositoryImpl
import com.ajaib.github.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val repository: UserRepositoryImpl
) : ViewModel() {

    private val _users = MutableStateFlow<Resource<List<UserDto>>>(Resource.Loading())
    val users: StateFlow<Resource<List<UserDto>>> = _users.asStateFlow()

    init {
        getUsers()
    }

    private fun getUsers() {
        viewModelScope.launch {
            repository.getUsers().collect { resource ->
                _users.value = resource
            }
        }
    }
}