import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.SolidColor
import kotlinx.coroutines.delay

// Matrix-inspired color theme
object MatrixTheme {
    val darkBackground = Color(0xFF0D0D0D)
    val matrixGreen = Color(0xFF00FF41)
    val matrixDarkGreen = Color(0xFF003B00)
    val accentPurple = Color(0xFF9E00FF)
    val matrixGlow = Color(0xFF39FF14)
    val darkSurface = Color(0xFF121212)
    val overlayBackground = Color(0x99000000)
    val codeFont = FontFamily.Monospace
}

/**
 * Main composable for showing the file upload dialog
 */
@Composable
fun FileUploadDialog(
    composeWindow: ComposeWindow,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onFilesSelected: (List<File>) -> Unit
) {

    // Animation states
    val density = LocalDensity.current
    var contentVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Start entrance animation when dialog becomes visible
    LaunchedEffect(isVisible) {
        if (isVisible) {
            contentVisible = false
            delay(100) // Small delay for smoother animation sequence
            contentVisible = true
            delay(300) // Delay before setting focus
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                // Handle potential focus exceptions
            }
        }
    }

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            // Modal overlay with semi-transparent background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MatrixTheme.overlayBackground)
                    .clickable(onClick = onDismiss)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Escape) {
                            onDismiss()
                            true
                        } else {
                            false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Animated dialog content
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = slideInVertically(
                        initialOffsetY = { with(density) { -40.dp.roundToPx() } },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(
                        targetOffsetY = { with(density) { -40.dp.roundToPx() } },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(700.dp)
                            .heightIn(min = 500.dp, max = 700.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .focusRequester(focusRequester)
                            .clickable(onClick = {}) // Prevent clicks from propagating to overlay
                    ) {
                        // Dialog content
                        FileUploadDialogContent(
                            composeWindow = composeWindow,
                            onDismiss = onDismiss,
                            onFilesSelected = onFilesSelected
                        )
                    }
                }
            }
        }
    }
}

/**
 * Internal content of the file upload dialog
 */
@Composable
private fun FileUploadDialogContent(
    composeWindow: ComposeWindow,
    onDismiss: () -> Unit,
    onFilesSelected: (List<File>) -> Unit
) {
    // State for tracking dropped files
    val droppedFiles = remember { mutableStateListOf<File>() }

    // State for tracking if we're currently dragging over the drop area
    val isDropTargetActive = remember { mutableStateOf(false) }

    // Register the drag and drop listener
    LaunchedEffect(Unit) {
        setupFileDragAndDrop(composeWindow,
            onDragEnter = { isDropTargetActive.value = true },
            onDragExit = { isDropTargetActive.value = false },
            onFilesDropped = { files ->
                droppedFiles.addAll(files)
                isDropTargetActive.value = false
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MatrixTheme.darkBackground,
        shape = RoundedCornerShape(12.dp),
        elevation = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FILE UPLOAD",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MatrixTheme.codeFont,
                    color = MatrixTheme.matrixGreen
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MatrixTheme.matrixGreen
                    )
                }
            }

            // Drop target area
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isDropTargetActive.value)
                           Modifier.background( Brush.radialGradient(
                               colors = listOf(
                                   MatrixTheme.matrixDarkGreen,
                                   MatrixTheme.darkBackground
                               ),
                               radius = 1000f
                           ))
                        else
                            Modifier.background(MatrixTheme.darkSurface)
                    )
                    .border(
                        width = if (isDropTargetActive.value) 2.dp else 1.dp,
                        color = if (isDropTargetActive.value) MatrixTheme.matrixGlow
                        else MatrixTheme.matrixDarkGreen,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = "Upload",
                        tint = if (isDropTargetActive.value) MatrixTheme.matrixGlow
                        else MatrixTheme.matrixGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isDropTargetActive.value)
                            "RELEASE TO UPLOAD"
                        else
                            "DRAG FILES HERE",
                        color = if (isDropTargetActive.value) MatrixTheme.matrixGlow
                        else MatrixTheme.matrixGreen,
                        fontFamily = MatrixTheme.codeFont,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // File section
            if (droppedFiles.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FILES: ${droppedFiles.size}",
                        fontSize = 16.sp,
                        fontFamily = MatrixTheme.codeFont,
                        color = MatrixTheme.matrixGreen
                    )

                    TextButton(
                        onClick = { droppedFiles.clear() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MatrixTheme.accentPurple
                        )
                    ) {
                        Text(
                            text = "CLEAR ALL",
                            fontFamily = MatrixTheme.codeFont,
                            fontSize = 12.sp
                        )
                    }
                }

                // File grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(droppedFiles) { file ->
                        FileGridItem(
                            file = file,
                            onRemove = { droppedFiles.remove(file) }
                        )
                    }
                }
            } else {
                // Empty state
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO FILES SELECTED",
                            color = MatrixTheme.matrixGreen.copy(alpha = 0.7f),
                            fontFamily = MatrixTheme.codeFont,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Bottom action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel button
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = Color.Transparent,
                        contentColor = MatrixTheme.matrixGreen
                    ),
                    border = ButtonDefaults.outlinedBorder.copy(
                        brush = SolidColor(MatrixTheme.matrixDarkGreen)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "CANCEL",
                        fontFamily = MatrixTheme.codeFont
                    )
                }

                // Upload button
                Button(
                    onClick = {
                        if (droppedFiles.isNotEmpty()) {
                            onFilesSelected(droppedFiles.toList())
                            onDismiss()
                        }
                    },
                    enabled = droppedFiles.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MatrixTheme.matrixDarkGreen,
                        contentColor = MatrixTheme.matrixGreen,
                        disabledBackgroundColor = MatrixTheme.matrixDarkGreen.copy(alpha = 0.3f),
                        disabledContentColor = MatrixTheme.matrixGreen.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "UPLOAD",
                        fontFamily = MatrixTheme.codeFont,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * File item for the grid view
 */
@Composable
private fun FileGridItem(file: File, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = 4.dp,
        backgroundColor = MatrixTheme.darkSurface,
        shape = RoundedCornerShape(6.dp)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // File type badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MatrixTheme.matrixDarkGreen)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = file.extension.uppercase().takeIf { it.isNotEmpty() } ?: "FILE",
                        color = MatrixTheme.matrixGreen,
                        fontFamily = MatrixTheme.codeFont,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // File name
                Text(
                    text = file.name,
                    fontWeight = FontWeight.Medium,
                    fontFamily = MatrixTheme.codeFont,
                    color = MatrixTheme.matrixGreen,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // File size
                Text(
                    text = formatFileSize(file.length()),
                    fontSize = 10.sp,
                    fontFamily = MatrixTheme.codeFont,
                    color = MatrixTheme.matrixGreen.copy(alpha = 0.7f)
                )
            }

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MatrixTheme.accentPurple,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Sets up drag and drop functionality
 */
private fun setupFileDragAndDrop(
    window: ComposeWindow,
    onDragEnter: () -> Unit,
    onDragExit: () -> Unit,
    onFilesDropped: (List<File>) -> Unit
) {
    val dropTarget = object : DropTarget() {
        override fun dragEnter(event: DropTargetDragEvent) {
            if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.acceptDrag(DnDConstants.ACTION_COPY)
                onDragEnter()
            } else {
                event.rejectDrag()
            }
        }

        override fun dragExit(event: DropTargetEvent) {
            onDragExit()
        }

        override fun drop(event: DropTargetDropEvent) {
            try {
                event.acceptDrop(DnDConstants.ACTION_COPY)

                @Suppress("UNCHECKED_CAST")
                val files = event.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>

                onFilesDropped(files)
                event.dropComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                event.dropComplete(false)
            }
        }
    }

    window.contentPane.dropTarget = dropTarget
}

/**
 * Formats file size in bytes to a human-readable string
 */
private fun formatFileSize(size: Long): String {
    val kilobyte = 1024L
    val megabyte = kilobyte * 1024
    val gigabyte = megabyte * 1024

    return when {
        size < kilobyte -> "$size B"
        size < megabyte -> String.format("%.1f KB", size.toFloat() / kilobyte)
        size < gigabyte -> String.format("%.1f MB", size.toFloat() / megabyte)
        else -> String.format("%.1f GB", size.toFloat() / gigabyte)
    }
}
