package com.example.bookshelf.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookshelf.BookshelfApplication
import com.example.bookshelf.data.BooksRepository
import com.example.bookshelf.network.BooksResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


sealed interface BooksUiState {
    data class Success(val booksResponse: BooksResponse, val hasNext: Boolean) : BooksUiState
    data class Error(val message: String) : BooksUiState
    data object Loading : BooksUiState
}

class BooksViewModel(private val repository: BooksRepository) : ViewModel() {

    var booksUiState: BooksUiState by mutableStateOf(BooksUiState.Loading)
        private set

    init {
        fetchBooks("book")
    }

    fun fetchBooks(query: String, page: Int = 0) {
        viewModelScope.launch {
            booksUiState = try {
                BooksUiState.Success(
                    booksResponse = repository.searchBooks(query, page),
                    hasNext = repository.searchBooks(query, (page + 1)).totalItems > (page+1) * 10,
                )
            } catch (e: IOException) {
                BooksUiState.Error("Network Error: ${e.message}")
            } catch (e: HttpException) {
                BooksUiState.Error("Server Error: ${e.message}")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as BookshelfApplication)
                BooksViewModel(repository = application.container.booksRepository)
            }
        }
    }
}