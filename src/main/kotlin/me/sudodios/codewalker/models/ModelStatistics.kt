package me.sudodios.codewalker.models

import me.sudodios.codewalker.utils.GsonUtils
import me.sudodios.codewalker.utils.GsonUtils.getArray
import me.sudodios.codewalker.utils.Utils.toValid

data class ModelStatisticsNative(
    var totalFilesCount : Long = 0,
    var totalCodeLinesCount : Long = 0,
    var totalCommentLinesCount : Long = 0,
    var totalBlankLinesCount : Long = 0,
    var totalFileTypesCount : Long = 0,
    var sizeOnDisk : Long = 0,
    var languages : Array<ModelLangStats>,
    var lastUpdateTime : Long
)

data class ModelStatistics(
    var id : Long? = null,
    var name : String = "",
    var root_folders : ArrayList<String> = arrayListOf(),
    var ignored_folders : ArrayList<String> = arrayListOf(),
    var last_update : Long = 0L,
    var configs : Configs = Configs(),
    var analyze : ArrayList<ModelLangStats> = arrayListOf(),
    var totals : Totals = Totals()
)

data class ModelStatisticsDB(
    var id : Long = -1,
    var name : String = "",
    var root_folders : String,
    var ignored_folders : String,
    var last_update : Long = 0L,
    var configs : String,
    var analyze : String,
    var totals : String
)

data class Configs (
    var hidden : Boolean = false,
    var no_ignore : Boolean = false,
    var doc_as_comment : Boolean = false,
)

data class Totals (
    var totalFilesCount : Long = 0,
    var totalCodeLinesCount : Long = 0,
    var totalCommentLinesCount : Long = 0,
    var totalBlankLinesCount : Long = 0,
    var totalFileTypesCount : Long = 0,
    var sizeOnDisk : Long = 0
)

fun ModelStatistics.forDB () : ModelStatisticsDB {
    return ModelStatisticsDB(
        id = this.id.toValid(),
        name = this.name,
        root_folders = GsonUtils.gson.toJson(this.root_folders),
        ignored_folders = GsonUtils.gson.toJson(this.ignored_folders),
        last_update = this.last_update,
        configs = GsonUtils.gson.toJson(this.configs),
        analyze = GsonUtils.gson.toJson(this.analyze),
        totals = GsonUtils.gson.toJson(this.totals)
    )
}

fun ModelStatisticsDB.forReal () : ModelStatistics {
    return ModelStatistics(
        id = this.id,
        name = this.name,
        root_folders = this.root_folders.getArray(String::class.java),
        ignored_folders = this.ignored_folders.getArray(String::class.java),
        last_update = this.last_update,
        configs = GsonUtils.gson.fromJson(this.configs,Configs::class.java),
        analyze = this.analyze.getArray(ModelLangStats::class.java),
        totals = GsonUtils.gson.fromJson(this.totals,Totals::class.java)
    )
}
