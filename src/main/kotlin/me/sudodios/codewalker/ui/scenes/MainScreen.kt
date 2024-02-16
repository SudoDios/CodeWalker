package me.sudodios.codewalker.ui.scenes

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.sudodios.codewalker.core.LibCore
import me.sudodios.codewalker.models.ModelLangStats
import me.sudodios.codewalker.models.ModelStatistics
import me.sudodios.codewalker.ui.components.*
import me.sudodios.codewalker.ui.components.menu.CustomDropdownMenu
import me.sudodios.codewalker.ui.components.menu.SortMenu
import me.sudodios.codewalker.ui.dialogs.BaseDialog
import me.sudodios.codewalker.ui.dialogs.CreateProjectDialog
import me.sudodios.codewalker.ui.scenes.sections.Drawer
import me.sudodios.codewalker.ui.theme.ColorTheme
import me.sudodios.codewalker.ui.theme.Fonts
import me.sudodios.codewalker.utils.GsonUtils
import me.sudodios.codewalker.utils.Utils.formatToPrice
import me.sudodios.codewalker.utils.Utils.formatToSizeFile
import me.sudodios.codewalker.utils.Utils.toColorInt
import me.sudodios.codewalker.utils.Utils.toTimeAgo
import moe.tlaster.precompose.navigation.Navigator

object MainScreen {

    val projectsList = SnapshotStateList<ModelStatistics>()
    var currentProject = mutableStateOf(ModelStatistics())

    fun setViewProject (modelStatistics: ModelStatistics?) {
        CoroutineScope(Dispatchers.IO).launch {
            if (modelStatistics == null) {
                currentProject.value = ModelStatistics()
                totalLinesChart.value = arrayListOf()
                totalLinesPerType.value = arrayListOf()
                totalFilesPerType.value = arrayListOf()
            } else {
                currentProject.value = modelStatistics
                totalLinesChart.value = arrayListOf(
                    LanguagesChart("Code lines", color = Color(0xFF807CF8), currentProject.value.totals.totalCodeLinesCount.toInt()),
                    LanguagesChart("Comment lines", color = Color(0xFFEC9966), currentProject.value.totals.totalCommentLinesCount.toInt()),
                    LanguagesChart("Blank lines", color = Color(0xFF4CC665), currentProject.value.totals.totalBlankLinesCount.toInt())
                )
                totalLinesPerType.value = ArrayList(currentProject.value.analyze.sortedByDescending { it.codeLinesCount }.take(10).map {
                    LanguagesChart(it.name, Color(it.color.toColorInt()),it.codeLinesCount)
                })
                totalFilesPerType.value = ArrayList(currentProject.value.analyze.sortedByDescending { it.filesCount }.take(10).map {
                    LanguagesChart(it.name, Color(it.color.toColorInt()),it.filesCount)
                })
            }
        }
    }

    //chart
    var totalLinesChart = mutableStateOf<ArrayList<LanguagesChart>>(arrayListOf())
    var totalLinesPerType = mutableStateOf<ArrayList<LanguagesChart>>(arrayListOf())
    var totalFilesPerType = mutableStateOf<ArrayList<LanguagesChart>>(arrayListOf())

}

@Composable
fun MainScreen(navigator: Navigator) {

    //edit ot create project
    var selectedProject by remember { mutableStateOf(ModelStatistics()) }
    var openProjectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        MainScreen.projectsList.addAll(LibCore.readProjectsList())
    }

    Row(Modifier.fillMaxSize().background(ColorTheme.colorPrimaryDark)) {
        Drawer(
            createOrEditProject = {
                selectedProject = it
                openProjectDialog = true
            }
        )
        Box(Modifier.fillMaxSize()) {
            ProjectNotSelectView(MainScreen.currentProject.value.id == null)
            AnalyzeContent(MainScreen.currentProject.value.id != null && MainScreen.currentProject.value.analyze.isNotEmpty(), onEditClicked = {
                selectedProject = MainScreen.currentProject.value.copy()
                openProjectDialog = true
            })
            ProjectEmptyView(MainScreen.currentProject.value.id != null && MainScreen.currentProject.value.analyze.isEmpty())
        }
    }

    //create or update project dialog
    BaseDialog(
        expanded = openProjectDialog,
        onDismissRequest = {
            openProjectDialog = false
        }
    ) {
        CreateProjectDialog(
            selectedProject,
            onCloseClicked = {
                openProjectDialog = false
            },
            onCreateOrUpdate = {
                openProjectDialog = false
            }
        )
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AnalyzeContent(show : Boolean,onEditClicked : () -> Unit) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val scrollState = rememberLazyListState()
        BoxWithConstraints(Modifier.fillMaxSize()) {
            LazyColumn(Modifier.areaBlur(size = Offset(maxWidth.value,125f)).fillMaxSize(), contentPadding = PaddingValues(top = 125.dp), state = scrollState, horizontalAlignment = Alignment.CenterHorizontally) {
                item {
                    TotalsView()
                }
                item {
                    Charts()
                }
                item {
                    LanguagesList()
                }
            }
            Column(modifier = Modifier.fillMaxWidth().height(125.dp).background(ColorTheme.colorCard1.copy(0.3f)), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    Modifier.widthIn(max = 1008.dp).fillMaxWidth().padding(start = 25.dp, end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(modifier = Modifier.weight(1f), targetState = MainScreen.currentProject.value.name) {
                        Txt(
                            text = it,
                            color = ColorTheme.colorText,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    if (MainScreen.currentProject.value.id != null && MainScreen.currentProject.value.id != -1L) {
                        MyIconButton(
                            icon = "icons/refresh.svg",
                            onClick = {
                                LibCore.getStats(
                                    projectName = MainScreen.currentProject.value.name,
                                    projectFolders = MainScreen.currentProject.value.root_folders,
                                    ignored = MainScreen.currentProject.value.ignored_folders,
                                    hidden = MainScreen.currentProject.value.configs.hidden,
                                    noIgnore = MainScreen.currentProject.value.configs.no_ignore,
                                    docAsComm = MainScreen.currentProject.value.configs.doc_as_comment,
                                    callback = {
                                        it.id = MainScreen.currentProject.value.id
                                        LibCore.refreshAnalyze(it.id!!,it.last_update,GsonUtils.gson.toJson(it.analyze),GsonUtils.gson.toJson(it.totals))
                                        val index = MainScreen.projectsList.indexOfFirst { d -> d.id == it.id }
                                        if (index != -1) {
                                            MainScreen.projectsList[index] = it
                                            MainScreen.setViewProject(it)
                                        }
                                    }
                                )
                            }
                        )
                        MyIconButton(
                            icon = "icons/edit.svg",
                            onClick = {
                                onEditClicked.invoke()
                            }
                        )
                    } else {
                        Icon(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(50)).background(ColorTheme.colorPrimary.copy(0.1f)).padding(10.dp),
                            painter = painterResource("icons/flame.svg"),
                            contentDescription = "flame-icon",
                            tint = ColorTheme.colorPrimary
                        )
                    }
                }
                Row(
                    Modifier.widthIn(max = 1008.dp).fillMaxWidth().padding(start = 25.dp, end = 25.dp, top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val desc = if (MainScreen.currentProject.value.id == -1L) {
                        MainScreen.currentProject.value.root_folders[0]
                    } else {
                        "${MainScreen.currentProject.value.root_folders.size} folders • ${MainScreen.currentProject.value.ignored_folders.size} ignore patterns"
                    }
                    TooltipArea(
                        tooltip =  {
                            TooltipC(MainScreen.currentProject.value.root_folders.joinToString("\n"))
                        }
                    ) {
                        Txt(
                            text = desc,
                            color = ColorTheme.colorText.copy(0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Box(Modifier.weight(1f))
                    val timeString = if (MainScreen.currentProject.value.id == -1L) {
                        "Quick Folder Analyze"
                    } else {
                        "Updated ${MainScreen.currentProject.value.last_update.toTimeAgo()}"
                    }
                    Txt(
                        text = timeString,
                        color = ColorTheme.colorText.copy(0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd),
                adapter = ScrollbarAdapter(scrollState),
                style = LocalScrollbarStyle.current.copy(hoverColor = ColorTheme.colorText.copy(0.6f), unhoverColor = ColorTheme.colorText.copy(.2f))
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TotalsView() {
    FlowRow(Modifier.padding(start = 20.dp,end = 20.dp, top = 35.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.Center) {
        FlowRow(maxItemsInEachRow = 3, horizontalArrangement = Arrangement.Center) {
            TotalItemView(
                icon = "icons/files-count.svg",
                text = "Files",
                value = MainScreen.currentProject.value.totals.totalFilesCount.formatToPrice()
            )
            TotalItemView(
                icon = "icons/code-lines.svg",
                text = "Code Lines",
                value = MainScreen.currentProject.value.totals.totalCodeLinesCount.formatToPrice()
            )
            TotalItemView(
                icon = "icons/comment-lines.svg",
                text = "Comment Lines",
                value = MainScreen.currentProject.value.totals.totalCommentLinesCount.formatToPrice()
            )
            TotalItemView(
                icon = "icons/blank-lines.svg",
                text = "Blank Lines",
                value = MainScreen.currentProject.value.totals.totalBlankLinesCount.formatToPrice()
            )
            TotalItemView(
                icon = "icons/file-types.svg",
                text = "File Types",
                value = MainScreen.currentProject.value.totals.totalFileTypesCount.formatToPrice()
            )
            TotalItemView(
                icon = "icons/size-on-disk.svg",
                text = "Size on Disk",
                value = MainScreen.currentProject.value.totals.sizeOnDisk.formatToSizeFile()
            )
        }
        Box(modifier = Modifier.padding(20.dp).size(392.dp),contentAlignment = Alignment.Center) {
            LanguagesChart(
                modifier = Modifier.fillMaxSize().padding(40.dp),
                data = MainScreen.totalLinesChart.value,
                filled = false
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val total = MainScreen.currentProject.value.totals.totalCodeLinesCount +
                        MainScreen.currentProject.value.totals.totalCommentLinesCount +
                        MainScreen.currentProject.value.totals.totalBlankLinesCount
                AnimatedContent(total) {
                    Txt(
                        modifier = Modifier.padding(top = 12.dp),
                        text = it.formatToPrice(),
                        color = ColorTheme.colorText,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Txt(
                    modifier = Modifier.padding(top = 6.dp),
                    text = "Total Lines",
                    color = ColorTheme.colorText.copy(0.7f),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Charts () {
    FlowRow(Modifier.padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.Center) {
        Column(modifier = Modifier.padding(12.dp).width(500.dp).clip(RoundedCornerShape(16.dp)).background(ColorTheme.colorCard2)
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Txt(
                text = "Total Lines per Type",
                color = ColorTheme.colorText.copy(0.8f),
                style = MaterialTheme.typography.titleMedium
            )
            AnimatedContent(MainScreen.totalLinesPerType.value) {
                LanguagesDotList(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                    data = it
                )
            }
            LanguagesChart(
                modifier = Modifier.padding(20.dp).size(392.dp).padding(40.dp),
                data = MainScreen.totalLinesPerType.value
            )
        }
        Column(modifier = Modifier.padding(12.dp).width(480.dp).clip(RoundedCornerShape(16.dp)).background(ColorTheme.colorCard2)
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Txt(
                text = "Total Files per Type",
                color = ColorTheme.colorText.copy(0.8f),
                style = MaterialTheme.typography.titleMedium
            )
            AnimatedContent(MainScreen.totalFilesPerType.value) {
                LanguagesDotList(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                    data = it
                )
            }
            LanguagesChart(
                modifier = Modifier.padding(20.dp).size(392.dp).padding(40.dp),
                data = MainScreen.totalFilesPerType.value
            )
        }
    }
}

@Composable
private fun LanguagesList () {

    var searchKeyword by remember { mutableStateOf("") }
    var isSortDesc by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var sort by remember { mutableStateOf("Files") }

    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
        .widthIn(max = 1008.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(ColorTheme.colorCard2).padding(bottom = 12.dp)) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.width(240.dp).clip(RoundedCornerShape(12.dp)).background(ColorTheme.colorPrimaryDark).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(24.dp).padding(3.dp),
                    painter = painterResource("icons/search-status.svg"),
                    contentDescription = "search-icon",
                    tint = ColorTheme.colorText.copy(0.8f)
                )
                Box(modifier = Modifier.padding(start = 12.dp).fillMaxWidth()) {
                    BasicTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = searchKeyword,
                        cursorBrush = SolidColor(ColorTheme.colorPrimary),
                        onValueChange = {
                            searchKeyword = it
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = Fonts.local_fount, color = ColorTheme.colorText, fontWeight = FontWeight.Thin)
                    )
                    this@Row.AnimatedVisibility(
                        visible = searchKeyword.isEmpty()
                    ) {
                        Txt(
                            text = "Search languages ..",
                            color = ColorTheme.colorText.copy(0.4f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Box(Modifier.weight(1f))
            Row(modifier = Modifier.padding(end = 12.dp).height(44.dp).clip(RoundedCornerShape(12.dp)).background(ColorTheme.colorPrimaryDark).animateContentSize().clickable {
                showSortMenu = true
            }.padding(start = 16.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Txt(
                    text = sort,
                    color = ColorTheme.colorText,
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    modifier = Modifier.padding(start = 12.dp).rotate(90f),
                    painter = painterResource("icons/arrow-right.svg"),
                    contentDescription = "arrow-icon",
                    tint = ColorTheme.colorText
                )
            }
            SortTypeSwitch(
                modifier = Modifier,
                isDesc = isSortDesc,
                onClick = {
                    isSortDesc = !isSortDesc
                }
            )
        }
        Row(modifier = Modifier.fillMaxWidth().height(64.dp).background(ColorTheme.colorCard1), verticalAlignment = Alignment.CenterVertically) {
            Dot(
                modifier = Modifier.padding(start = 16.dp).size(8.dp),
                color = Color.Transparent
            )
            Txt(
                modifier = Modifier.padding(start = 16.dp).weight(1.8f),
                text = "Languages",
                color = ColorTheme.colorPrimary,
                style = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Start)
            )
            Txt(
                modifier = Modifier.weight(1f),
                text = "Files",
                color = ColorTheme.colorPrimary,
                style = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Start)
            )
            Txt(
                modifier = Modifier.weight(1f),
                text = "Lines",
                color = ColorTheme.colorPrimary,
                style = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Start)
            )
            Txt(
                modifier = Modifier.weight(1f),
                text = "Code",
                color = ColorTheme.colorPrimary,
                style = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Start)
            )
            Txt(
                modifier = Modifier.weight(1f),
                text = "Comments",
                color = ColorTheme.colorPrimary,
                style = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Start)
            )
            Txt(
                modifier = Modifier.weight(1f),
                text = "Blanks",
                color = ColorTheme.colorPrimary,
                style = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Start)
            )
        }
        getLanguagesList(sort,isSortDesc,searchKeyword).forEach {
            Row(modifier = Modifier.fillMaxWidth().height(54.dp), verticalAlignment = Alignment.CenterVertically) {
                Dot(
                    modifier = Modifier.padding(start = 16.dp).size(8.dp),
                    color = Color(it.color.toColorInt())
                )
                Txt(
                    modifier = Modifier.padding(start = 16.dp).weight(1.8f),
                    text = it.name,
                    color = ColorTheme.colorText,
                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start)
                )
                Txt(
                    modifier = Modifier.weight(1f),
                    text = it.filesCount.toLong().formatToPrice(),
                    color = ColorTheme.colorText,
                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start)
                )
                Txt(
                    modifier = Modifier.weight(1f),
                    text = it.totalLinesCount.toLong().formatToPrice(),
                    color = ColorTheme.colorText,
                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start)
                )
                Txt(
                    modifier = Modifier.weight(1f),
                    text = it.codeLinesCount.toLong().formatToPrice(),
                    color = ColorTheme.colorText,
                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start)
                )
                Txt(
                    modifier = Modifier.weight(1f),
                    text = it.commentLinesCount.toLong().formatToPrice(),
                    color = ColorTheme.colorText,
                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start)
                )
                Txt(
                    modifier = Modifier.weight(1f),
                    text = it.blankLinesCount.toLong().formatToPrice(),
                    color = ColorTheme.colorText,
                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start)
                )
            }
        }
    }

    CustomDropdownMenu(
        expanded = showSortMenu,
        onDismissRequest = {
            showSortMenu = false
        }
    ) {
        SortMenu(
            selectedItem = sort,
            onClicked = {
                showSortMenu = false
                sort = it
            }
        )
    }
}

@Composable
private fun TotalItemView(icon: String, text: String, value: String) {
    Column(
        Modifier.padding(8.dp).size(180.dp).clip(RoundedCornerShape(12.dp)).background(ColorTheme.colorCard2),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(48.dp),
            painter = painterResource(icon),
            contentDescription = null
        )
        AnimatedContent(value) {
            Txt(
                modifier = Modifier.padding(top = 12.dp),
                text = it,
                color = ColorTheme.colorText,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Txt(
            modifier = Modifier.padding(top = 6.dp),
            text = text,
            color = ColorTheme.colorText.copy(0.7f),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun ProjectNotSelectView(show: Boolean) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(90.dp),
                painter = painterResource("icons/chart-3.svg"),
                contentDescription = "chart-icon",
                tint = ColorTheme.colorText.copy(0.5f)
            )
            Txt(
                modifier = Modifier.padding(top = 20.dp).clip(RoundedCornerShape(12.dp))
                    .background(ColorTheme.colorText.copy(0.05f))
                    .padding(top = 6.dp, bottom = 6.dp, start = 8.dp, end = 8.dp),
                text = "Please select an project to view analyze here",
                color = ColorTheme.colorText.copy(0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ProjectEmptyView(show: Boolean) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(90.dp),
                painter = painterResource("icons/folder.svg"),
                contentDescription = "chart-icon",
                tint = ColorTheme.colorText.copy(0.5f)
            )
            val stringBuilder = StringBuilder()
            stringBuilder.append("This folder(s) is empty from source codes\n")
            stringBuilder.append("Please choice another folder for your project")
            MainScreen.currentProject.value.root_folders.forEach {
                stringBuilder.append("\n$it")
            }
            Txt(
                modifier = Modifier.padding(top = 20.dp).clip(RoundedCornerShape(12.dp))
                    .background(ColorTheme.colorText.copy(0.05f))
                    .padding(top = 6.dp, bottom = 6.dp, start = 8.dp, end = 8.dp),
                text = stringBuilder.toString(),
                color = ColorTheme.colorText.copy(0.6f),
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
            )
        }
    }
}


/*fns*/
private fun getLanguagesList (sort : String,sortIsDesc : Boolean,filterKey : String) : List<ModelLangStats> {
    return if (sortIsDesc) {
        MainScreen.currentProject.value.analyze.filter { it.name.lowercase().contains(filterKey.lowercase()) }.sortedByDescending {
            when (sort) {
                "Files" -> it.filesCount
                "Lines" -> it.totalLinesCount
                "Codes" -> it.codeLinesCount
                "Comments" -> it.commentLinesCount
                "Blanks" -> it.blankLinesCount
                else -> it.filesCount
            }
        }
    } else {
        MainScreen.currentProject.value.analyze.filter { it.name.lowercase().contains(filterKey.lowercase()) }.sortedBy {
            when (sort) {
                "Files" -> it.filesCount
                "Lines" -> it.totalLinesCount
                "Codes" -> it.codeLinesCount
                "Comments" -> it.commentLinesCount
                "Blanks" -> it.blankLinesCount
                else -> it.filesCount
            }
        }
    }
}