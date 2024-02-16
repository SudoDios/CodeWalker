package me.sudodios.codewalker.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.sudodios.codewalker.core.LibCore
import me.sudodios.codewalker.models.ModelStatistics
import me.sudodios.codewalker.models.forDB
import me.sudodios.codewalker.ui.components.*
import me.sudodios.codewalker.ui.scenes.MainScreen
import me.sudodios.codewalker.ui.theme.ColorTheme
import java.io.File

enum class CreateFrags(var desc : String) {
    Root("Root"),
    Folders("Choice project folders"),
    IgnorePatterns("Ignore file patterns"),
    FilterLanguages("Filter languages"),
    Configs("Other configs")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateProjectDialog(
    modelStatistics: ModelStatistics,
    onCloseClicked : () -> Unit,
    onCreateOrUpdate : () -> Unit
) {
    var currentFrag by remember { mutableStateOf(CreateFrags.Root) }

    var projectName by remember { mutableStateOf("") }
    val projectFolders = rememberMutableStateListOf<String>()
    val ignorePatterns = rememberMutableStateListOf<String>()

    var searchHidden by remember { mutableStateOf(false) }
    var noIgnore by remember { mutableStateOf(false) }
    var docAsComm by remember { mutableStateOf(false) }

    LaunchedEffect(modelStatistics) {
        projectName = modelStatistics.name
        projectFolders.clear()
        projectFolders.addAll(modelStatistics.root_folders)
        ignorePatterns.clear()
        ignorePatterns.addAll(modelStatistics.ignored_folders)
        searchHidden = modelStatistics.configs.hidden
        noIgnore = modelStatistics.configs.no_ignore
        docAsComm = modelStatistics.configs.doc_as_comment
    }

    fun createOrUpdate () {
        LibCore.getStats(
            projectName = projectName,
            projectFolders = ArrayList(projectFolders),
            ignored = ArrayList(ignorePatterns),
            hidden = searchHidden,
            noIgnore = noIgnore,
            docAsComm = docAsComm,
            callback = {
                if (modelStatistics.id != null) {
                    //update
                    it.id = modelStatistics.id
                    LibCore.updateProject(it.forDB())
                    val index = MainScreen.projectsList.indexOfFirst { d -> d.id == it.id }
                    if (index != -1) {
                        MainScreen.projectsList[index] = it
                    }
                    if (MainScreen.currentProject.value.id == it.id) {
                        MainScreen.setViewProject(it)
                    }
                } else {
                    //create
                    val insertId = LibCore.createProject(it.forDB())
                    it.id = insertId
                    MainScreen.projectsList.add(it)
                    MainScreen.setViewProject(it)
                }
                onCreateOrUpdate.invoke()
            }
        )
    }

    Column(modifier = Modifier.width(480.dp).animateContentSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth().height(64.dp).background(ColorTheme.colorPrimaryDark).padding(start = 8.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            AnimatedContent(if (currentFrag == CreateFrags.Root) "icons/close.svg" else "icons/arrow-left.svg") {
                MyIconButton(
                    icon = it,
                    onClick = {
                        if (currentFrag == CreateFrags.Root) {
                            onCloseClicked.invoke()
                        } else {
                            currentFrag = CreateFrags.Root
                        }
                    }
                )
            }
            val title = if (currentFrag == CreateFrags.Root) if (modelStatistics.id == null) "Create new project" else "Update project" else currentFrag.desc
            Txt(
                modifier = Modifier.padding(start = 12.dp).weight(1f),
                text = title,
                color = ColorTheme.colorText.copy(0.8f),
                style = MaterialTheme.typography.titleMedium
            )
            AnimatedVisibility(
                visible = currentFrag == CreateFrags.Root,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Button(
                    contentPadding = PaddingValues(start = 8.dp, end = 12.dp),
                    enabled = projectName.trim().isNotEmpty() && projectFolders.isNotEmpty(),
                    onClick = {
                        createOrUpdate()
                    }
                ) {
                    Icon(
                        painter = painterResource("icons/tick-circle.svg"),
                        contentDescription = "tick-icon",
                        tint = ColorTheme.colorPrimaryDark
                    )
                    Txt(
                        modifier = Modifier.padding(start = 8.dp),
                        text = if (modelStatistics.id == null) "Create" else "Update",
                        color = ColorTheme.colorPrimaryDark,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        when (currentFrag) {
            CreateFrags.Root -> {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TxtField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Project name",
                        value = projectName,
                        onValueChange = {
                            projectName = it
                        }
                    )
                    ConfItem(
                        modifier = Modifier.padding(top = 16.dp),
                        icon = "icons/folder-add.svg",
                        text = "Project folders",
                        showBadge = projectFolders.isEmpty(),
                        onClicked = {
                            currentFrag = CreateFrags.Folders
                        }
                    )
                    /*ConfItem(
                        modifier = Modifier.padding(top = 16.dp),
                        icon = "icons/filter-search.svg",
                        text = "Filter languages",
                        onClicked = {
                            currentFrag = CreateFrags.FilterLanguages
                        }
                    )*/
                    ConfItem(
                        modifier = Modifier.padding(top = 16.dp),
                        icon = "icons/code-circle.svg",
                        text = "Ignored patterns",
                        showBadge = false,
                        onClicked = {
                            currentFrag = CreateFrags.IgnorePatterns
                        }
                    )
                    ConfItem(
                        modifier = Modifier.padding(top = 16.dp),
                        icon = "icons/setting-4.svg",
                        text = "Other configs",
                        showBadge = false,
                        onClicked = {
                            currentFrag = CreateFrags.Configs
                        }
                    )
                }
            }
            CreateFrags.Folders -> {
                FolderFrag(projectFolders)
            }
            CreateFrags.FilterLanguages -> {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    var isChecked by remember { mutableStateOf(false) }
                    FlowRow(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        supported_languages.lines().forEach {
                            CBoxButton(
                                text = it,
                                isChecked = isChecked,
                                onClicked = {
                                    isChecked = !isChecked
                                }
                            )
                        }
                    }

                }
            }
            CreateFrags.IgnorePatterns -> {
                IgnorePatternFrag(ignorePatterns)
            }
            CreateFrags.Configs -> {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CBoxButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Search Hidden Files",
                        isChecked = searchHidden,
                        onClicked = {
                            searchHidden = !searchHidden
                        }
                    )
                    CBoxButton(
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                        text = "Don't respect ignore files",
                        isChecked = noIgnore,
                        onClicked = {
                            noIgnore = !noIgnore
                        }
                    )
                    CBoxButton(
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                        text = "treat doc strings in languages as comments",
                        isChecked = docAsComm,
                        onClicked = {
                            docAsComm = !docAsComm
                        }
                    )
                }
            }
        }
    }

}

@Composable
private fun ConfItem (
    modifier: Modifier = Modifier,
    showBadge : Boolean,
    icon : String,
    text : String,
    onClicked : () -> Unit
) {
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(ColorTheme.colorPrimaryDark.copy(0.3f)).clickable {
        onClicked.invoke()
    }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = "code-icon",
            tint = ColorTheme.colorText.copy(0.8f)
        )
        Txt(
            modifier = Modifier.padding(start = 12.dp).weight(1f),
            text = text,
            color = ColorTheme.colorText.copy(0.8f),
            style = MaterialTheme.typography.bodyMedium
        )
        AnimatedVisibility(
            visible = showBadge
        ) {
            Dot(modifier = Modifier.padding(end = 8.dp).size(7.dp), color = ColorTheme.colorPrimary)
        }
        Icon(
            painter = painterResource("icons/arrow-right.svg"),
            contentDescription = "arrow-icon",
            tint = ColorTheme.colorText.copy(0.6f)
        )
    }
}

@Composable
private fun FolderFrag (projectFolders : SnapshotStateList<String>) {

    var showFolderPicker by remember { mutableStateOf(false) }

    FolderPickerDialog(
        show = showFolderPicker,
        onCloseRequest = {
            showFolderPicker = false
        },
        onSelectFolder = {
            showFolderPicker = false
            if (!projectFolders.contains(it)) {
                projectFolders.add(it)
            }
        }
    )

    Row(Modifier.fillMaxWidth().background(ColorTheme.colorText.copy(0.1f)).clickable {
        showFolderPicker = true
    }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource("icons/folder-add.svg"),
            contentDescription = "folder-add",
            tint = ColorTheme.colorText.copy(0.8f)
        )
        Txt(
            modifier = Modifier.padding(start = 16.dp),
            text = "Add Folder",
            color = ColorTheme.colorText.copy(0.8f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
    if (projectFolders.isEmpty()) {
        Txt(
            modifier = Modifier.padding(30.dp),
            text = "Please add your project folders",
            color = ColorTheme.colorText.copy(0.6f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
    if (projectFolders.isNotEmpty()) {
        LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)) {
            itemsIndexed(projectFolders) { index, item ->
                Row(modifier = Modifier.padding(top = 3.dp, bottom = 3.dp).fillMaxWidth().background(ColorTheme.colorCard1).padding(start = 20.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource("icons/folder.svg"),
                        contentDescription = "folder-icon",
                        tint = ColorTheme.colorText.copy(0.7f)
                    )
                    Column(Modifier.padding(start = 16.dp).weight(1f)) {
                        Txt(
                            text = item.substringAfterLast(File.separator),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ColorTheme.colorText.copy(.9f)
                        )
                        Txt(
                            modifier = Modifier.padding(top = 3.dp),
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorTheme.colorText.copy(.7f)
                        )
                    }
                    MyIconButton(
                        icon = "icons/close-circle.svg",
                        onClick = {
                            projectFolders.remove(item)
                        }
                    )
                }
                if (index < projectFolders.lastIndex) {
                    Divider(Modifier.fillMaxWidth(), color = ColorTheme.colorText.copy(0.1f))
                }
            }
        }
    }
}

@Composable
private fun IgnorePatternFrag(ignorePatterns : SnapshotStateList<String>) {

    var showAddDialog by remember { mutableStateOf(false) }

    BaseDialog(
        expanded = showAddDialog,
        onDismissRequest = {
            showAddDialog = false
        }
    ) {
        AddPatternDialog(
            onCloseRequest = {
                showAddDialog = false
            },
            onSelectPattern = {
                showAddDialog = false
                if (!ignorePatterns.contains(it)) {
                    ignorePatterns.add(it)
                }
            }
        )
    }

    Row(Modifier.fillMaxWidth().background(ColorTheme.colorText.copy(0.1f)).clickable {
        showAddDialog = true
    }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource("icons/add-circle.svg"),
            contentDescription = "icon-add",
            tint = ColorTheme.colorText.copy(0.8f)
        )
        Txt(
            modifier = Modifier.padding(start = 16.dp),
            text = "Add regex pattern",
            color = ColorTheme.colorText.copy(0.8f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
    if (ignorePatterns.isEmpty()) {
        Txt(
            modifier = Modifier.padding(30.dp),
            text = "Please add ignore patterns like (*.svg)",
            color = ColorTheme.colorText.copy(0.6f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
    if (ignorePatterns.isNotEmpty()) {
        LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)) {
            itemsIndexed(ignorePatterns) { index, item ->
                Row(modifier = Modifier.padding(top = 3.dp, bottom = 3.dp).fillMaxWidth().background(ColorTheme.colorCard1).padding(start = 16.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Txt(
                        modifier = Modifier.weight(1f),
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorTheme.colorText.copy(.9f)
                    )
                    MyIconButton(
                        icon = "icons/close-circle.svg",
                        onClick = {
                            ignorePatterns.remove(item)
                        }
                    )
                }
                if (index < ignorePatterns.lastIndex) {
                    Divider(Modifier.fillMaxWidth(), color = ColorTheme.colorText.copy(0.1f))
                }
            }
        }
    }
}

private val supported_languages = """
    abnf
    awk
    abap
    actionscript
    ada
    agda
    alex
    alloy
    arduino c++
    asciidoc
    asn.1
    asp
    asp.net
    assembly
    gnu style assembly
    ats
    autohotkey
    autoconf
    autoit
    automake
    bash
    batch
    bean
    bitbake
    brightscript
    c
    c header
    cmake
    c#
    c shell
    cabal
    cassius
    ceylon
    clojure
    clojurec
    clojurescript
    cobol
    codeql
    coffeescript
    cogent
    coldfusion
    coldfusion cfscript
    coq
    c++
    c++ header
    crystal
    css
    cuda
    cython
    d
    daml
    dart
    device tree
    dhall
    dockerfile
    .net resource
    dream maker
    dust.js
    ebuild
    edn
    emacs lisp
    elixir
    elm
    elvish
    emacs dev env
    emojicode
    erlang
    fen
    f#
    factor
    fennel
    fish
    flatbuffers schema
    forge config
    forth
    fortran legacy
    fortran modern
    freemarker
    f*
    futhark
    gdb script
    gdscript
    gherkin (cucumber)
    gleam
    glsl
    gml
    go
    go html
    graphql
    groovy
    gwion
    haml
    hamlet
    handlebars
    happy
    haskell
    haxe
    hcl
    headache
    hex
    hlsl
    holyc
    html
    hy
    idris
    ini
    intel hex
    isabelle
    jai
    java
    javascript
    jinja2
    json
    jsonnet
    jsx
    julia
    julius
    jupyter notebooks
    k
    kakoune script
    kotlin
    korn shell
    kv language
    llvm
    lean
    less
    ld script
    liquid
    common lisp
    livescript
    logtalk
    lolcode
    lua
    lucius
    m4
    madlang
    makefile
    markdown
    meson
    metal shading language
    mint
    mlatu
    module-definition
    moonscript
    msbuild
    mustache
    nextflow
    nim
    nix
    not quite perl
    ocaml
    objective-c
    objective-c++
    odin
    open policy agent
    opentype feature file
    org
    oz
    psl assertion
    pan
    pascal
    perl
    pest
    php
    poke
    polly
    pony
    postcss
    powershell
    processing
    prolog
    protocol buffers
    pug
    puppet
    purescript
    python
    q
    qcl
    qml
    r
    rusty object notation
    rpm specfile
    racket
    rakefile
    raku
    razor
    rescript
    restructuredtext
    ren'py
    ruby
    ruby html
    rust
    srecode template
    sass
    scala
    scheme
    scons
    shell
    shaderlab
    standard ml (sml)
    solidity
    specman e
    spice netlist
    sqf
    sql
    stan
    stratego/xt
    stylus
    svelte
    svg
    swift
    swig
    systemverilog
    tcl
    tera
    tex
    plain text
    thrift
    toml
    tsx
    ttcn-3
    twig
    typescript
    umpl
    unison
    unreal markdown
    unreal plugin
    unreal project
    unreal script
    unreal shader
    unreal shader header
    ur/web
    ur/web project
    vb6
    vbscript
    vala
    apache velocity
    verilog
    verilog args file
    vhdl
    vim script
    visual basic
    visual studio project
    visual studio solution
    vue
    webgpu shader language
    webassembly
    the wenyan programming language
    wolfram
    xsl
    xaml
    xcode config
    xml
    xtend
    yaml
    zencode
    zig
    zsh
""".trimIndent()