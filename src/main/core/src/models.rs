#[allow(non_snake_case)]
pub struct ModelLangStats {
    pub name : String,
    pub color : String,
    pub filesCount : usize,
    pub totalLinesCount : usize,
    pub codeLinesCount : usize,
    pub commentLinesCount : usize,
    pub blankLinesCount : usize,
}

#[allow(non_snake_case)]
#[derive(Debug)]
pub struct ModelStatisticsDB {
    pub id : usize,
    pub name : String,
    pub root_folders : String,
    pub ignored_folders : String,
    pub last_update : usize,
    pub configs : String,
    pub analyze : String,
    pub totals : String,
}

#[allow(non_snake_case)]
pub struct ModelStatistics {
    pub totalFilesCount : usize,
    pub totalCodeLinesCount : usize,
    pub totalCommentLinesCount : usize,
    pub totalBlankLinesCount : usize,
    pub totalFileTypesCount : usize,
    pub sizeOnDisk : usize,
    pub languages : Vec<ModelLangStats>,
    pub lastUpdateTime : u128,
}