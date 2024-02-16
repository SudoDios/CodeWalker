use std::collections::HashMap;

pub struct ColorFinder {
    color_map : HashMap<String,String>
}

impl ColorFinder {
    pub fn init () -> Self {
        let mut color_map: HashMap<String, String> = HashMap::new();
        let raw = include_str!("../rsc/langs-color-map.txt");
        for line in raw.lines() {
            let parts = line.splitn(2, ":").collect::<Vec<_>>();
            let name = parts[0].trim().to_owned();
            let hex_code = parts[1].trim().to_owned();
            color_map.insert(name.to_lowercase(), hex_code);
        }
        Self {
            color_map
        }
    }
    pub fn get_color_by_lang_name (&self,name : &str) -> String {
        self.color_map.get(name.to_lowercase().trim()).map(|value| value.clone()).unwrap_or_else(|| "#EDEDED".to_string())
    }
}