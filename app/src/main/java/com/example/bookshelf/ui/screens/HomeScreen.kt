package com.example.bookshelf.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.bookshelf.R
import com.example.bookshelf.network.BookItem
import com.example.bookshelf.ui.BooksUiState
import com.example.bookshelf.ui.theme.BookShelfTheme

@Composable
fun HomeScreen(
    booksUiState: BooksUiState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    fetch: (String, Int) -> Unit
) {
    when (booksUiState) {
        is BooksUiState.Error -> ErrorScreen(booksUiState.message, modifier.fillMaxSize())
        is BooksUiState.Loading -> LoadingScreen(modifier.fillMaxSize())
        is BooksUiState.Success -> BooksGridScreen(
            hasNext = booksUiState.hasNext,
            booksList = booksUiState.booksResponse.items,
            modifier = modifier,
            fetch = fetch,
            contentPadding = contentPadding,
        )
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = stringResource(R.string.loading)
    )
}

@Composable
fun ErrorScreen(msg: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_connection_error),
            contentDescription = stringResource(R.string.connection_error)
        )
        Text(
            text = stringResource(R.string.loading_failed) + msg,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun BooksGridScreen(
    hasNext: Boolean,
    booksList: List<BookItem>?,
    fetch: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var currentPage by remember { mutableIntStateOf(0) }
    var currentQuery by remember { mutableStateOf("book") }
    val gridState = rememberLazyGridState()
    LaunchedEffect(currentPage) {
        gridState.scrollToItem(0)
    }
    Column {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            modifier = modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(booksList ?: emptyList()) { book ->
                BookCard(
                    book,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )
            }
        }
        if (booksList != null) {
            Controls(
                currentQuery = currentQuery,
                onQueryChange = { currentQuery = it },
                currentPage = currentPage + 1,
                onPreviousClick = {
                    if (currentPage > 0) {
                        currentPage--
                        fetch("book", currentPage)
                    }
                },
                onNextClick = {
                    currentPage++
                    fetch("book", currentPage)
                },
                isPreviousEnabled = currentPage > 0,
                isNextEnabled = hasNext,
                search = { q ->
                    currentPage = 0
                    fetch(q, currentPage)

                },
                modifier = Modifier
            )
        }
    }
}


@Composable
fun BookCard(bookItem: BookItem, modifier: Modifier = Modifier) {
    Card(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        modifier = modifier.heightIn(max = 200.dp)
    ) {

        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current)
                .data(bookItem.volumeInfo.imageLinks?.thumbnail?.replace("http", "https"))
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.placeholder_result),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.loading_img),
            error = painterResource(R.drawable.ic_broken_image),
            modifier = Modifier.fillMaxSize()
        )


    }
}

@Composable
fun Controls(
    currentQuery: String,
    onQueryChange: (String) -> Unit,
    currentPage: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    isPreviousEnabled: Boolean,
    isNextEnabled: Boolean,
    search: (String) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.primary)
            .fillMaxWidth(),
    ) {
        SearchBox(currentQuery, onQueryChange, search)
        PaginationControls(
            currentPage = currentPage,
            onPreviousClick = onPreviousClick,
            onNextClick = onNextClick,
            isPreviousEnabled = isPreviousEnabled,
            isNextEnabled = isNextEnabled
        )
    }

}

@Composable
fun SearchBox(
    currentQuery: String,
    onValueChange: (String) -> Unit,
    search: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        TextField(
            currentQuery,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.titleSmall,
            shape = RoundedCornerShape(16.dp),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    search(currentQuery)
                }
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
        )
        OutlinedButton(
            onClick = { search(currentQuery) },
            enabled = currentQuery.isNotBlank(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("Search", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    isPreviousEnabled: Boolean,
    isNextEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onPreviousClick,
            enabled = isPreviousEnabled,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("Previous", style = MaterialTheme.typography.titleSmall)
        }

        Row(
            modifier = Modifier.weight(1.5f),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Page $currentPage",
                color = MaterialTheme.colorScheme.onTertiary,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        OutlinedButton(
            onClick = onNextClick,
            enabled = isNextEnabled,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("Next", style = MaterialTheme.typography.titleSmall)
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun LoadingScreenPreview() {
    BookShelfTheme {
        LoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorScreenPreview() {
    BookShelfTheme {
        ErrorScreen("")
    }
}