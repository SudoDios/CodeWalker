use jni::JNIEnv;
use jni::objects::{JClass, JObject, JObjectArray, JString};
use jni::sys::{jboolean, jlong, jobject, jobjectArray, jstring};
use tokei::{Config, Languages};

use crate::colors::ColorFinder;
use crate::jni_utils::{convert_data_class_to_model_db, convert_java_array_to_vec, convert_model_stat_to_object};
use crate::models::{ModelLangStats, ModelStatistics};
use crate::util::{get_current_time_millis, get_sum_of_dirs_sizes};

mod models;
mod colors;
mod util;
mod database;
mod jni_utils;

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_codewalker_core_LibCore_version<'local>(env: JNIEnv<'local>, _class: JClass<'local>) -> jstring {
    env.new_string("1.0.0-a").unwrap().into_raw()
}

/*db*/
#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_codewalker_core_LibCore_initDB<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,dbPath : JString<'local>) {
    let db_path : String = env.get_string(&dbPath).expect("").into();
    database::init_db(db_path.as_str());
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_codewalker_core_LibCore_createProject<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,modelP : JObject<'local>) -> jlong {
    let dbModel = convert_data_class_to_model_db(&mut env, &modelP);
    let insertedId = database::create_project(&dbModel);
    return insertedId as jlong
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_codewalker_core_LibCore_updateProject<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,modelP : JObject<'local>) {
    let dbModel = convert_data_class_to_model_db(&mut env,&modelP);
    database::update_project(dbModel)
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_codewalker_core_LibCore_refreshAnalyze<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                        pId : jlong,updateTime : jlong,languages : JString<'local>,totals : JString<'local>) {
    let _languages: String = env.get_string(&languages).expect("").into();
    let _totals: String = env.get_string(&totals).expect("").into();
    database::refresh_analyze(pId as usize,updateTime as usize,_languages.as_str(),_totals.as_str())
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_codewalker_core_LibCore_removeProject<'local>(_: JNIEnv<'local>, _class: JClass<'local>, pId : jlong) {
    database::remove_project(pId as usize)
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_codewalker_core_LibCore_readProjects<'local>(env: JNIEnv<'local>, _class: JClass<'local>) -> jobjectArray {
    let projectsList = database::read_projects();
    return jni_utils::convert_vec_to_j_o_array(env,projectsList).into_raw()
}
/*end db*/


#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn Java_me_sudodios_codewalker_core_LibCore_getDirCodeStats<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>,
                                                                                        folders : JObjectArray<'local>,ignored : JObjectArray<'local>,
                                                                                        hidden : jboolean,noIgnore : jboolean,docAsComm : jboolean) -> jobject {
    //parse
    let _folders = convert_java_array_to_vec(&mut env, &folders);
    let _ignored = convert_java_array_to_vec(&mut env, &ignored);

    let _hidden: bool = if hidden == 0 { false } else { true };
    let _noIgnore: bool = if noIgnore == 0 { false } else { true };
    let _docAsComm: bool = if docAsComm == 0 { false } else { true };

    //conf & start
    let languages = init_lib(_folders.as_slice(),_ignored.iter().map(|s| s.as_str()).collect::<Vec<_>>().as_slice(),_hidden,_noIgnore,_docAsComm);
    let colorFinder = ColorFinder::init();
    let mut langResults: Vec<ModelLangStats> = Vec::new();
    for lang in languages.iter() {
        let res = ModelLangStats {
            name: lang.0.name().parse().unwrap(),
            color: colorFinder.get_color_by_lang_name(lang.0.name()),
            filesCount: lang.1.reports.len(),
            totalLinesCount: lang.1.lines(),
            codeLinesCount: lang.1.code,
            commentLinesCount: lang.1.comments,
            blankLinesCount: lang.1.blanks
        };
        langResults.push(res)
    }

    let totalFilesCount : usize = langResults.iter().map(|s| s.filesCount).sum();
    let totalCodeLinesCount : usize = langResults.iter().map(|s| s.codeLinesCount).sum();
    let totalCommentLinesCount : usize = langResults.iter().map(|s| s.commentLinesCount).sum();
    let totalBlankLinesCount : usize = langResults.iter().map(|s| s.blankLinesCount).sum();
    let totalFileTypesCount : usize = langResults.len();

    let statModel = ModelStatistics {
        totalFilesCount,
        totalCodeLinesCount,
        totalCommentLinesCount,
        totalBlankLinesCount,
        totalFileTypesCount,
        sizeOnDisk: get_sum_of_dirs_sizes(&_folders),
        languages: langResults,
        lastUpdateTime: get_current_time_millis()
    };
    let convert = convert_model_stat_to_object(env,statModel);
    convert.into_raw()
}

fn init_lib(paths : &[String],ignore : &[&str],hidden : bool,no_ignore : bool,doc_as_comment : bool) -> Languages {
    let config = Config {
        columns: None,
        hidden : Option::from(hidden),
        no_ignore: Option::from(no_ignore),
        no_ignore_parent: None,
        no_ignore_dot: None,
        no_ignore_vcs: None,
        treat_doc_strings_as_comments: Option::from(doc_as_comment),
        sort: None,
        types: None,
        for_each_fn: None,
    };
    let mut languages = Languages::new();
    languages.get_statistics(paths, ignore, &config);
    languages
}