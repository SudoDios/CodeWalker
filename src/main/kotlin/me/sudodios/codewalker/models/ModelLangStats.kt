package me.sudodios.codewalker.models

data class ModelLangStats(
    var name : String = "",
    var color : String = "",
    var filesCount : Int = 0,
    var totalLinesCount : Int = 0,
    var codeLinesCount : Int = 0,
    var commentLinesCount : Int = 0,
    var blankLinesCount : Int = 0
)
