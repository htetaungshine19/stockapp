package com.plcoding.stockmarketapp.presentation.company_listing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.stockmarketapp.domain.repository.StockRepository
import com.plcoding.stockmarketapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyListingViewModel @Inject constructor(
    private val repository: StockRepository,
) : ViewModel() {


    private var _state by mutableStateOf(CompanyListingState())
    val state: CompanyListingState
        get() {
            return _state
        }
    private var searchJob: Job? = null

    fun onEvent(event: CompanyListingEvent) {
        when (event) {
            is CompanyListingEvent.Refresh -> {
                getCompanyListings(fetchFromRemote = true)
            }
            is CompanyListingEvent.OnSearchQueryChange -> {
                _state = _state.copy(
                    searchQuery = event.query
                )
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500)
                    getCompanyListings()
                }
            }
        }
    }

    private fun getCompanyListings(
        query: String = _state.searchQuery.lowercase(),
        fetchFromRemote: Boolean = false,
    ) {

        viewModelScope.launch {
            repository.getCompanyListings(fetchFromRemote, query).collect {
                when (it) {
                    is Resource.Success -> {
                        it.data?.let { listings ->
                            _state.copy(companies = listings)
                        }
                    }
                    is Resource.Loading -> {
                        _state = _state.copy(isLoading = true)

                    }
                    is Resource.Error -> {
                    }
                }
            }
        }


    }
}