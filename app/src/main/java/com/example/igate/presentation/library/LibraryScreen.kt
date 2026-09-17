package com.example.igate.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igate.IgateApplication
import com.example.igate.domain.model.Note
import com.example.igate.domain.model.NoteType
import com.example.igate.presentation.IgateViewModelFactory
import com.example.igate.presentation.common.UiState
import com.example.igate.presentation.common.ShimmerList

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(
        factory = IgateViewModelFactory((LocalContext.current.applicationContext as IgateApplication).container.igateRepository)
    ),
    onPdfClick: (String, String) -> Unit = { _, _ -> }
) {
    val allNotesState by viewModel.allNotes.collectAsState()
    val savedDocumentIds by viewModel.savedDocumentIds.collectAsState()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Explore", "Saved")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Text(
            text = "Library",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(24.dp)
        )

        // Custom Minimalist Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .height(3.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(MaterialTheme.colorScheme.tertiary)
                )
            },
            divider = {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { 
                        Text(
                            text = title, 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    selectedContentColor = MaterialTheme.colorScheme.tertiary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = allNotesState) {
                is UiState.Loading, is UiState.Idle -> {
                    Box(modifier = Modifier.padding(24.dp)) {
                        ShimmerList(count = 4)
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    val documentsToShow = if (selectedTabIndex == 0) {
                        state.data
                    } else {
                        state.data.filter { savedDocumentIds.contains(it.id) }
                    }
                    
                    if (documentsToShow.isEmpty() && selectedTabIndex == 1) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No saved documents yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        DocumentList(
                            documents = documentsToShow,
                            savedIds = savedDocumentIds,
                            onToggleBookmark = { viewModel.toggleBookmark(it) },
                            onPdfClick = onPdfClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentList(
    documents: List<Note>,
    savedIds: Set<String>,
    onToggleBookmark: (String) -> Unit,
    onPdfClick: (String, String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(documents) { document ->
            val isPdf = document.type == NoteType.PDF
            DocumentCard(
                title = document.title,
                description = document.description,
                isPdf = isPdf,
                isSaved = savedIds.contains(document.id),
                onClick = {
                    if (isPdf) {
                        onPdfClick(document.fileUrlOrContent, document.title)
                    }
                },
                onToggleBookmark = { onToggleBookmark(document.id) }
            )
        }
    }
}

@Composable
fun DocumentCard(
    title: String, 
    description: String, 
    isPdf: Boolean, 
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPdf) Icons.Filled.PictureAsPdf else Icons.Filled.Description,
                contentDescription = null,
                tint = if (isPdf) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggleBookmark) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
