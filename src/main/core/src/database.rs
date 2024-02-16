use std::string::ToString;
use rusqlite::Connection;
use crate::models::ModelStatisticsDB;

const TABLE_NAME: &str = "projects";
static mut DB_PATH: String = String::new();

pub(crate) fn init_db (path : &str) {
    unsafe {
        DB_PATH = path.to_string();
    }
    let conn = Connection::open(path).unwrap();
    conn.execute(format!("CREATE TABLE IF NOT EXISTS {} (
                          id INTEGER PRIMARY KEY AUTOINCREMENT,
                          name TEXT,
                          root_folders TEXT,
                          ignored_folders TEXT,
                          last_update INTEGER,
                          configs TEXT,
                          analyze TEXT,
                          totals TEXT)"
                         ,TABLE_NAME).as_str(),()).unwrap();
    conn.close().unwrap();
}

pub(crate) fn create_project (model_statistics_db: &ModelStatisticsDB) -> i64 {
    let conn = Connection::open(unsafe { &DB_PATH }).unwrap();
    conn.execute(format!("INSERT INTO {} (name,root_folders,ignored_folders,last_update,configs,analyze,totals)
                          VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)"
         ,TABLE_NAME).as_str(),(
        &model_statistics_db.name,
        &model_statistics_db.root_folders,
        &model_statistics_db.ignored_folders,
        &model_statistics_db.last_update,
        &model_statistics_db.configs,
        &model_statistics_db.analyze,
        &model_statistics_db.totals,
    )).unwrap();
    let inserted_id = conn.last_insert_rowid();
    conn.close().unwrap();
    return inserted_id;
}

pub(crate) fn update_project (model_statistics_db: ModelStatisticsDB) {
    let conn = Connection::open(unsafe { &DB_PATH }).unwrap();
    conn.execute(format!("UPDATE {} set name=?1,root_folders=?2,ignored_folders=?3,last_update=?4,configs=?5,analyze=?6,totals=?7
                          WHERE id=?8"
                         ,TABLE_NAME).as_str(),(
        &model_statistics_db.name,
        &model_statistics_db.root_folders,
        &model_statistics_db.ignored_folders,
        &model_statistics_db.last_update,
        &model_statistics_db.configs,
        &model_statistics_db.analyze,
        &model_statistics_db.totals,
        &model_statistics_db.id
    )).unwrap();
    conn.close().unwrap();
}

pub(crate) fn refresh_analyze (p_id : usize,update_time : usize,languages : &str,totals : &str) {
    let conn = Connection::open(unsafe { &DB_PATH }).unwrap();
    conn.execute(format!("UPDATE {} set last_update=?1,analyze=?2,totals=?3 WHERE id=?4",TABLE_NAME).as_str(),(
        &update_time,
        &languages,
        &totals,
        &p_id
    )).unwrap();
    conn.close().unwrap();
}

pub(crate) fn remove_project (p_id : usize) {
    let conn = Connection::open(unsafe { &DB_PATH }).unwrap();
    conn.execute(format!("DELETE FROM {} WHERE id={}",TABLE_NAME,p_id).as_str(),()).unwrap();
    conn.close().unwrap();
}

pub(crate) fn read_projects () -> Vec<ModelStatisticsDB> {
    let conn = Connection::open(unsafe { &DB_PATH }).unwrap();
    let mut result_out : Vec<ModelStatisticsDB> = Vec::new();
    {
        let mut statment = conn.prepare(format!("SELECT * FROM {} ORDER BY last_update DESC",TABLE_NAME).as_str()).unwrap();
        let result_iter = statment.query_map([], |row| {
            Ok(ModelStatisticsDB {
                id: row.get(0)?,
                name: row.get(1)?,
                root_folders: row.get(2)?,
                ignored_folders: row.get(3)?,
                last_update: row.get(4)?,
                configs: row.get(5)?,
                analyze: row.get(6)?,
                totals: row.get(7)?,
            })
        }).unwrap();
        for item in result_iter {
            result_out.push(item.unwrap())
        }
    }
    conn.close().unwrap();
    return result_out
}