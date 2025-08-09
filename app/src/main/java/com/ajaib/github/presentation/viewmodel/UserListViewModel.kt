package com.ajaib.github.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajaib.github.domain.model.User
import com.ajaib.github.domain.usecase.GetUsersUseCase
import com.ajaib.github.domain.usecase.SearchUsersUseCase
import com.ajaib.github.utils.Constants
import com.ajaib.github.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserListViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val searchUsersUseCase: SearchUsersUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val users: StateFlow<Resource<List<User>>> = _searchQuery
        .debounce(Constants.SEARCH_DELAY_MS)
        .filter { it.length >= Constants.MIN_SEARCH_QUERY_LENGTH  }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                getUsersUseCase()
            } else {
                searchUsersUseCase(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Re-trigger the flow by emitting the current search query
            _searchQuery.value = _searchQuery.value
            _isRefreshing.value = false
        }
    }
}