package me.sudodios.codewalker.ui.scenes.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.sudodios.codewalker.core.LibCore
import me.sudodios.codewalker.models.ModelStatistics
import me.sudodios.codewalker.ui.components.*
import me.sudodios.codewalker.ui.dialogs.BaseDialog
import me.sudodios.codewalker.ui.dialogs.DeleteDialog
import me.sudodios.codewalker.ui.dialogs.FolderPickerDialog
import me.sudodios.codewalker.ui.scenes.MainScreen
import me.sudodios.codewalker.ui.theme.ColorTheme
import me.sudodios.codewalker.utils.Utils.toColorInt
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Drawer(
    createOrEditProject : (ModelStatistics) -> Unit
) {

    var showQuickFolderPicker by remember { mutableStateOf(false) }

    Box(
        Modifier.fillMaxHeight().width(310.dp).background(ColorTheme.colorCard1)
    ) {
        EmptyListView(MainScreen.projectsList.isEmpty())
        ProjectList(MainScreen.projectsList)
        Column(modifier = Modifier.fillMaxWidth().clickable(),horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth().height(72.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource("icons/app-icon-solid.png"),
                    contentDescription = "app-icon"
                )
                Txt(
                    modifier = Modifier.padding(start = 16.dp),
                    text = "Code Walker",
                    color = ColorTheme.colorText,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Divider(Modifier.fillMaxWidth(), color = ColorTheme.colorText.copy(0.1f))
            Row(Modifier.padding(top = 20.dp, bottom = 20.dp).fillMaxWidth(0.9f), verticalAlignment = Alignment.CenterVertically) {
                GradientButton(
                    modifier = Modifier.padding(end = 12.dp).weight(1f),
                    text = "Add New Project",
                    onClicked = {
                        createOrEditProject.invoke(ModelStatistics())
                    }
                )
                TooltipArea(
                    tooltip = {
                        TooltipC("Quick code analyze")
                    }
                ) {
                    MyIconButton(
                        size = 40.dp,
                        contentPadding = 8.dp,
                        background = ColorTheme.colorPrimary.copy(0.1f),
                        colorFilter = ColorTheme.colorPrimary,
                        icon = "icons/flame.svg",
                        onClick = {
                            showQuickFolderPicker = true
                        }
                    )
                }
            }
        }
    }

    //fast analyze
    FolderPickerDialog(
        show = showQuickFolderPicker,
        onCloseRequest = {
            showQuickFolderPicker = false
        },
        onSelectFolder = {
            showQuickFolderPicker = false
            LibCore.getStats(
                projectName = it.substringAfterLast(File.separator),
                projectFolders = arrayListOf(it),
                ignored = arrayListOf(),
                hidden = false,
                noIgnore = false,
                docAsComm = false,
                callback = { result ->
                    result.id = -1
                    MainScreen.setViewProject(result)
                }
            )
        }
    )

}

@Composable
private fun EmptyListView(listEmpty: Boolean) {
    AnimatedVisibility(
        visible = listEmpty,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(70.dp),
                painter = painterResource("icons/empty-folder.svg"),
                contentDescription = "empty-list",
                tint = ColorTheme.colorText.copy(0.7f)
            )
            Txt(
                modifier = Modifier.padding(top = 16.dp),
                text = "No projects !",
                color = ColorTheme.colorText.copy(0.8f),
                style = MaterialTheme.typography.titleSmall
            )
            Txt(
                modifier = Modifier.padding(top = 8.dp, bottom = 26.dp),
                text = "Please add your project",
                color = ColorTheme.colorText.copy(0.6f),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ProjectList (projectsList: SnapshotStateList<ModelStatistics>) {

    var showDelDialog by remember { mutableStateOf(false) }
    var selForDelItem by remember { mutableStateOf<ModelStatistics?>(null) }

    AnimatedVisibility(
        visible = projectsList.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LazyColumn(Modifier.areaBlur(size = Offset(310f,152f)).fillMaxWidth(), contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 165.dp, bottom = 12.dp)) {
            items(projectsList.sortedByDescending { it.last_update }) {
                val bgAnimation = if (MainScreen.currentProject.value.id == it.id) ColorTheme.colorText.copy(0.2f) else ColorTheme.colorCard2
                Column(Modifier.padding(6.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgAnimation).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    MainScreen.setViewProject(it)
                }.pointerHoverIcon(PointerIcon.Hand)) {
                    Row(modifier = Modifier.padding(top = 10.dp, end = 10.dp, start = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        AnimatedVisibility(
                            visible = MainScreen.currentProject.value.id == it.id
                        ) {
                            Dot(modifier = Modifier.padding(end = 12.dp).size(6.dp), color = ColorTheme.colorPrimary)
                        }
                        Txt(
                            text = it.name,
                            color = ColorTheme.colorText,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Row(modifier = Modifier.padding(start = 12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Txt(
                            modifier = Modifier.weight(1f),
                            text = "${it.root_folders.size} folders",
                            color = ColorTheme.colorText.copy(0.7f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        MyIconButton(
                            size = 44.dp,
                            contentPadding = 10.dp,
                            icon = "icons/trash.svg",
                            onClick = {
                                selForDelItem = it
                                showDelDialog = true
                            }
                        )
                    }
                    val dataForChart = it.analyze.sortedByDescending { d -> d.codeLinesCount }.take(5)
                    LineSegmentChart(
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        data = ArrayList(dataForChart.map { d -> LanguagesChart(d.name, Color(d.color.toColorInt()),d.codeLinesCount) })
                    )
                }
            }
        }
    }

    BaseDialog(
        expanded = showDelDialog,
        onDismissRequest = {
            showDelDialog = false
        }
    ) {
        DeleteDialog(
            projectName = selForDelItem?.name.toString(),
            onCancelClicked = {
                showDelDialog = false
            },
            onOKClicked = {
                showDelDialog = false
                LibCore.removeProject(selForDelItem?.id!!)
                val index = MainScreen.projectsList.indexOfFirst { it.id == selForDelItem?.id }
                if (MainScreen.currentProject.value.id == selForDelItem?.id) {
                    MainScreen.setViewProject(null)
                }
                if (index != -1) {
                    MainScreen.projectsList.removeAt(index)
                }
            }
        )
    }

}