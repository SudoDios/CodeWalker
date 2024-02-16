package me.sudodios.codewalker.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.sudodios.codewalker.core.Global
import me.sudodios.codewalker.ui.components.Txt
import me.sudodios.codewalker.ui.components.rememberMutableStateListOf
import me.sudodios.codewalker.ui.theme.ColorTheme
import java.io.File
import java.io.FileFilter

@Composable
fun FolderPickerDialog(
    show: Boolean,
    onCloseRequest: () -> Unit,
    onSelectFolder : (String) -> Unit
) {

    BaseDialog(
        expanded = show,
        onDismissRequest = onCloseRequest
    ) {

        var selectedFolder by remember { mutableStateOf<String?>(null) }

        val listOfFolders = rememberMutableStateListOf<FolderItem>()

        LaunchedEffect(Unit) {
            val folders = getListOfDirs(Global.userHome)
            listOfFolders.addAll(folders!!)
        }

        Column(Modifier.width(440.dp).heightIn(min = 490.dp, max = 580.dp).background(ColorTheme.colorPrimaryDark)) {
            DialogToolbar(
                title = "Choose Folder",
                content = {
                    Button(
                        enabled = selectedFolder != null,
                        onClick = {
                            onSelectFolder.invoke(selectedFolder!!)
                        }
                    ) {
                        Txt("Accept Folder")
                    }
                },
                onCloseClicked = {
                    onCloseRequest.invoke()
                }
            )
            AnimatedVisibility(
                visible = selectedFolder != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(Modifier.fillMaxWidth().height(44.dp).background(ColorTheme.colorCard1).padding(start = 16.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AnimatedContent(selectedFolder) {
                        Txt(
                            text = it.toString(),
                            color = ColorTheme.colorPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Box(Modifier.fillMaxSize()) {
                val verticalScrollState = rememberScrollState()
                val horizontalScrollState = rememberScrollState()
                Column(
                    modifier = Modifier.verticalScroll(verticalScrollState).horizontalScroll(horizontalScrollState)
                        .width(IntrinsicSize.Max)
                ) {
                    Spacer(Modifier.padding(4.dp))
                    RootNode(listOfFolders, selectedFolder.toString()) {
                        selectedFolder = it
                    }
                    Spacer(Modifier.padding(8.dp))
                }
                VerticalScrollbar(modifier = Modifier.align(Alignment.CenterEnd), adapter = ScrollbarAdapter(verticalScrollState),
                    style = LocalScrollbarStyle.current.copy(hoverColor = ColorTheme.colorText.copy(0.8f), unhoverColor = ColorTheme.colorText.copy(0.2f)))
                HorizontalScrollbar(modifier = Modifier.align(Alignment.BottomCenter), adapter = ScrollbarAdapter(horizontalScrollState),
                    style = LocalScrollbarStyle.current.copy(hoverColor = ColorTheme.colorText.copy(0.8f), unhoverColor = ColorTheme.colorText.copy(0.2f)))
            }
        }

    }

}

private data class FolderItem(
    var path: String,
    var name: String,
    var expanded: MutableState<Boolean> = mutableStateOf(false),
    var childs: SnapshotStateList<FolderItem>
)

private fun getListOfDirs(dir: String): List<FolderItem>? {
    return File(dir).listFiles(FileFilter { it.isDirectory && !it.isHidden })?.sortedBy { it.name }?.map {
        FolderItem(path = it.absolutePath, name = it.name, childs = SnapshotStateList())
    }
}

private fun getDepthItem (path : String) : Int {
    return path.replace("${Global.userHome}${File.separator}", "").count { d -> d == File.separator.single() }
}

@Composable
private fun RootNode(
    nodes: SnapshotStateList<FolderItem>,
    selectedPath: String,
    onClicked: (String) -> Unit
) {
    nodes.forEach {
        FolderItem(
            selectedPath = selectedPath,
            folderItem = it,
            depth = getDepthItem(it.path),
            onClicked = {
                onClicked.invoke(it.path)
            },
            onDoubleClicked = {
                if (it.childs.isEmpty()) {
                    it.childs.addAll(getListOfDirs(it.path)!!)
                }
                it.expanded.value = !it.expanded.value
                it.childs.forEach { child ->
                    child.expanded.value = false
                }
            }
        )
        AnimatedVisibility(
            visible = it.expanded.value,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                RootNode(it.childs, selectedPath, onClicked)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FolderItem(
    selectedPath: String,
    depth: Int,
    folderItem: FolderItem,
    onClicked: () -> Unit,
    onDoubleClicked: () -> Unit
) {

    val expandAnimation = animateFloatAsState(if (folderItem.expanded.value) 90f else 0f)

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = ((depth * 20) + 12).dp, end = 12.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(50)).clickable { onDoubleClicked.invoke() }
                .padding(5.dp)
                .rotate(expandAnimation.value),
            painter = painterResource("icons/arrow-right.svg"),
            contentDescription = "arrow-icon",
            tint = ColorTheme.colorText.copy(0.7f)
        )
        Row(
            Modifier.weight(1f).onPointerEvent(PointerEventType.Press) {
                when {
                    it.buttons.isPrimaryPressed -> when (it.awtEventOrNull?.clickCount) {
                        1 -> onClicked.invoke()
                        2 -> onDoubleClicked.invoke()
                    }
                }
            }.clip(RoundedCornerShape(12.dp))
                .background(if (selectedPath == folderItem.path) ColorTheme.colorText.copy(0.1f) else Color.Transparent)
                .padding(10.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource("icons/folder.svg"),
                contentDescription = "folder-icon",
                tint = ColorTheme.colorText.copy(0.7f)
            )
            Txt(
                modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                text = folderItem.name,
                color = ColorTheme.colorText,
                style = MaterialTheme.typography.bodyMedium
            )
        }

    }
}