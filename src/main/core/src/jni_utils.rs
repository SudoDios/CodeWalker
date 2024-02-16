use jni::JNIEnv;
use jni::objects::{JObject, JObjectArray, JString, JValue};
use jni::sys::{jint, jlong, jsize};
use crate::models::{ModelStatistics, ModelStatisticsDB};

pub(crate) fn convert_data_class_to_model_db(env: &mut JNIEnv, input : &JObject) -> ModelStatisticsDB {
    let id: jlong = env.get_field(input, "id", "J").unwrap().j().unwrap();
    let name: JString = JString::from(env.get_field(input, "name", "Ljava/lang/String;").unwrap().l().unwrap());
    let root_folders: JString = JString::from(env.get_field(input, "root_folders", "Ljava/lang/String;").unwrap().l().unwrap());
    let ignored_folders: JString = JString::from(env.get_field(input, "ignored_folders", "Ljava/lang/String;").unwrap().l().unwrap());
    let last_update: jlong = env.get_field(input, "last_update", "J").unwrap().j().unwrap();
    let configs: JString = JString::from(env.get_field(input, "configs", "Ljava/lang/String;").unwrap().l().unwrap());
    let analyze: JString = JString::from(env.get_field(input, "analyze", "Ljava/lang/String;").unwrap().l().unwrap());
    let totals: JString = JString::from(env.get_field(input, "totals", "Ljava/lang/String;").unwrap().l().unwrap());
    let _name : String = env.get_string(&name).expect("").into();
    let _root_folders : String = env.get_string(&root_folders).expect("").into();
    let _ignored_folders : String = env.get_string(&ignored_folders).expect("").into();
    let _configs : String = env.get_string(&configs).expect("").into();
    let _analyze : String = env.get_string(&analyze).expect("").into();
    let _totals : String = env.get_string(&totals).expect("").into();
    return ModelStatisticsDB {
        id : id as usize,
        name: _name,
        root_folders:_root_folders,
        ignored_folders: _ignored_folders,
        last_update: last_update as usize,
        configs: _configs,
        analyze: _analyze,
        totals: _totals
    }
}

pub(crate) fn convert_vec_to_j_o_array (mut env : JNIEnv,input_vec : Vec<ModelStatisticsDB>) -> JObjectArray {
    let kt_model_db_stats = env.find_class("me/sudodios/codewalker/models/ModelStatisticsDB").unwrap();
    let array = env.new_object_array(input_vec.len() as i32, &kt_model_db_stats, JObject::null()).unwrap();
    for (i, item) in input_vec.iter().enumerate() {
        let array_ref = &array;
        let item_obj = env.new_object(&kt_model_db_stats,
                                      "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                                      &[
                                          JValue::Long(item.id as jlong),
                                          JValue::Object(&*env.new_string(item.name.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(item.root_folders.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(item.ignored_folders.clone()).unwrap()),
                                          JValue::Long(item.last_update as jlong),
                                          JValue::Object(&*env.new_string(item.configs.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(item.analyze.clone()).unwrap()),
                                          JValue::Object(&*env.new_string(item.totals.clone()).unwrap())
                                      ]
        ).unwrap();
        env.set_object_array_element(array_ref, i as i32, item_obj).unwrap();
    }
    array
}

pub(crate) fn convert_java_array_to_vec (env: &mut JNIEnv, input : &JObjectArray) -> Vec<String> {
    let length = env.get_array_length(input).unwrap() as usize;
    let mut strings: Vec<String> = Vec::with_capacity(length);
    for i in 0..length {
        let j_object = env.get_object_array_element(input,i as jsize).unwrap();
        let j_str = JString::from(j_object);
        let input: String = env.get_string(&j_str).unwrap().into();
        strings.push(input);
    }
    strings
}

pub(crate) fn convert_model_stat_to_object (mut env: JNIEnv, model_statistics: ModelStatistics) -> JObject {
    let kt_lang_stat_class = env.find_class("me/sudodios/codewalker/models/ModelLangStats").unwrap();
    let array = env.new_object_array(model_statistics.languages.len() as i32, &kt_lang_stat_class, JObject::null()).unwrap();
    for (i, lang) in model_statistics.languages.iter().enumerate() {
        let array_ref = &array;
        let kt_lang_stat = env.new_object(&kt_lang_stat_class, "(Ljava/lang/String;Ljava/lang/String;IIIII)V", &[
            JValue::Object(&*env.new_string(lang.name.clone()).unwrap()),
            JValue::Object(&*env.new_string(lang.color.clone()).unwrap()),
            JValue::Int(lang.filesCount as jint),
            JValue::Int(lang.totalLinesCount as jint),
            JValue::Int(lang.codeLinesCount as jint),
            JValue::Int(lang.commentLinesCount as jint),
            JValue::Int(lang.blankLinesCount as jint),
        ]).unwrap();
        env.set_object_array_element(array_ref, i as i32, kt_lang_stat).unwrap();
    }
    let kt_model_stats = env.find_class("me/sudodios/codewalker/models/ModelStatisticsNative").unwrap();
    let kt_model_obj = env.new_object(kt_model_stats, "(JJJJJJ[Lme/sudodios/codewalker/models/ModelLangStats;J)V", &[
        JValue::Long(model_statistics.totalFilesCount as jlong),
        JValue::Long(model_statistics.totalCodeLinesCount as jlong),
        JValue::Long(model_statistics.totalCommentLinesCount as jlong),
        JValue::Long(model_statistics.totalBlankLinesCount as jlong),
        JValue::Long(model_statistics.totalFileTypesCount as jlong),
        JValue::Long(model_statistics.sizeOnDisk as jlong),
        JValue::Object(&*array),
        JValue::Long(model_statistics.lastUpdateTime.clone() as jlong),
    ]).unwrap();
    kt_model_obj
}