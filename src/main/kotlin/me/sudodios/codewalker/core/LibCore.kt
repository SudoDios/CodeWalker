package me.sudodios.codewalker.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.sudodios.codewalker.models.*
import java.io.File

object LibCore {

    /*
    * extern core lib func
    */

    external fun version () : String
    private external fun getDirCodeStats (folders : Array<String>,ignored : Array<String>,hidden : Boolean,noIgnore : Boolean,docAsComm : Boolean) : ModelStatisticsNative

    /*db*/
    external fun initDB (dbPath : String)
    external fun createProject (modelStatisticsDB: ModelStatisticsDB) : Long
    external fun updateProject (modelStatisticsDB: ModelStatisticsDB)
    external fun refreshAnalyze (pId: Long,updateTime : Long,languages : String,totals : String)
    external fun removeProject (pId : Long)
    private external fun readProjects () : Array<ModelStatisticsDB>

    fun readProjectsList () : ArrayList<ModelStatistics> {
        val out = ArrayList<ModelStatistics>()
        readProjects().forEach {
            out.add(it.forReal())
        }
        return out
    }

    fun getStats (projectName : String,projectFolders : ArrayList<String>,ignored : ArrayList<String>,hidden : Boolean,noIgnore : Boolean,docAsComm : Boolean,callback : (ModelStatistics) -> Unit) {
        Global.Alert.showLoading("getStats")
        CoroutineScope(Dispatchers.IO).launch {
            projectFolders.removeAll { it.isEmpty() }
            ignored.removeAll { it.trim().isEmpty() }
            val analyze = getDirCodeStats(folders = projectFolders.toTypedArray(), ignored = ignored.toTypedArray(), hidden = hidden, noIgnore = noIgnore, docAsComm = docAsComm)
            val resultStat = ModelStatistics(
                name = projectName,
                root_folders = projectFolders,
                ignored_folders = ignored,
                configs = Configs(hidden = hidden, no_ignore = noIgnore, doc_as_comment = docAsComm),
                last_update = analyze.lastUpdateTime,
                analyze = ArrayList(analyze.languages.toList()),
                totals = Totals(
                    totalFilesCount = analyze.totalFilesCount,
                    totalCodeLinesCount = analyze.totalCodeLinesCount,
                    totalCommentLinesCount = analyze.totalCommentLinesCount,
                    totalBlankLinesCount = analyze.totalBlankLinesCount,
                    totalFileTypesCount = analyze.totalFileTypesCount,
                    sizeOnDisk = analyze.sizeOnDisk,
                )
            )
            Global.Alert.hideLoading("getStats")
            callback.invoke(resultStat)
        }
    }

    fun init () : Boolean {
        File(Global.LIB_CORE_PATH).mkdirs()
        val libPath = findLibPath()
        return if (libPath == null) { false } else {
            if (File(libPath).exists()) {
                System.load(libPath)
                true
            } else {
                false
            }
        }
    }

    private fun findLibPath () : String? {
        val libExt = getLibExt()
        if (libExt == null) { return null } else {
            val libCoreRes = javaClass.classLoader.getResourceAsStream(libName)!!
            File("${Global.LIB_CORE_PATH}/$libName").writeBytes(libCoreRes.readAllBytes())
            return "${Global.LIB_CORE_PATH}/$libName"
        }
    }

    private val libName : String = "${getLibName()}.${getLibExt()}"
    private fun getLibExt(): String? {
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.contains("win") -> "dll"
            os.contains("nix") || os.contains("nux") || os.contains("aix") -> "so"
            os.contains("mac") -> "dylib"
            else -> null
        }
    }

    private fun getLibName(): String? {
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.contains("win") -> "core_code_walker"
            os.contains("nix") || os.contains("nux") || os.contains("aix") || os.contains("mac") -> "libcore_code_walker"
            else -> null
        }
    }

}