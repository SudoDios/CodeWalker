use std::path::Path;
use std::time::{SystemTime, UNIX_EPOCH};
use fs_extra::dir::get_size;

pub(crate) fn get_current_time_millis() -> u128 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_millis()
}

pub(crate) fn get_sum_of_dirs_sizes (folders : &Vec<String>) -> usize {
    let mut result : usize = 0;
    for folder in folders {
        if Path::new(folder).exists() {
            result += get_size(folder).unwrap() as usize
        }
    }
    result
}