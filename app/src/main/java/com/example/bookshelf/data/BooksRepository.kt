package com.example.bookshelf.data

import com.example.bookshelf.network.BooksApiService
import com.example.bookshelf.network.BooksResponse

interface BooksRepository{
    suspend fun searchBooks(query: String, page: Int = 0): BooksResponse
}

class NetworkBooksRepository (private val booksApiService: BooksApiService): BooksRepository{
    override suspend fun searchBooks(query: String, page: Int): BooksResponse = booksApiService.searchBooks(query, startIndex = page*10)
}