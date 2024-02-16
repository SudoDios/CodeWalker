use arbitrary::Arbitrary;

/// Represents a individual programming language. Can be used to provide
/// information about the language, such as multi line comments, single line
/// comments, string literal syntax, whether a given language allows nesting
/// comments.
#[derive(Deserialize)]
#[derive(Arbitrary, Clone, Copy, Debug, Eq, Hash, Ord, PartialEq, PartialOrd)]
#[non_exhaustive]
#[allow(clippy::upper_case_acronyms)]
pub enum LanguageType {
    {% for key, value in languages -%}
        #[allow(missing_docs)] {% if value.name is defined %} #[serde(alias = "{{value.name}}")] {% else %} #[serde(alias = "{{key}}")] {% endif %} {{key}},
    {% endfor %}
}

impl LanguageType {

    /// Returns the display name of a language.
    ///
    /// ```
    /// # use tokei::*;
    /// let bash = LanguageType::Bash;
    ///
    /// assert_eq!(bash.name(), "BASH");
    /// ```
    pub fn name(self) -> &'static str {
        match self {
            {% for key, value in languages -%}
                {{key}} => {% if value.name %}"{{value.name}}"{% else %}"{{key}}"{% endif %},
            {% endfor %}
        }
    }

    pub(crate) fn _is_blank(self) -> bool {
        match self {
            {% for key, v in languages -%}
                {{key}} => {{ v.blank | default(value=false) }},
            {% endfor %}
        }
    }

    pub(crate) fn is_fortran(self) -> bool {
        self == LanguageType::FortranModern ||
        self == LanguageType::FortranLegacy
    }

    /// Returns whether the language is "literate", meaning that it considered
    /// to primarily be documentation and is counted primarily as comments
    /// rather than procedural code.
    pub fn is_literate(self) -> bool {
        match self {
            {% for key, v in languages -%}
                {{key}} => {{ v.literate | default(value=false) }},
            {% endfor %}
        }
    }

    /// Provides every variant in a Vec
    pub fn list() -> &'static [(Self, &'static [&'static str])] {
        &[{% for key, val in languages -%}
            ({{key}},
            {% if val.extensions %} &[{% for extension in val.extensions %}"{{extension}}", {% endfor %}],
            {% else %} &[],
            {% endif %}),
        {% endfor %}]
    }

    /// Returns the single line comments of a language.
    /// ```
    /// use tokei::LanguageType;
    /// let lang = LanguageType::Rust;
    /// assert_eq!(lang.line_comments(), &["//"]);
    /// ```
    pub fn line_comments(self) -> &'static [&'static str] {
        match self {
            {% for key, value in languages -%}
                {{key}} => &[{% for item in value.line_comment | default(value=[]) %}"{{item}}",{% endfor %}],
            {% endfor %}
        }
    }

    /// Returns the single line comments of a language.
    /// ```
    /// use tokei::LanguageType;
    /// let lang = LanguageType::Rust;
    /// assert_eq!(lang.multi_line_comments(), &[("/*", "*/")]);
    /// ```
    pub fn multi_line_comments(self) -> &'static [(&'static str, &'static str)]
    {
        match self {
            {% for key, value in languages -%}
                {{key}} => &[
                    {%- for items in value.multi_line_comments | default(value=[]) -%}
                        ({% for item in items %}"{{item}}",{% endfor %}),
                    {%- endfor -%}
                ],
            {% endfor %}
        }
    }


    /// Returns whether the language allows nested multi line comments.
    /// ```
    /// use tokei::LanguageType;
    /// let lang = LanguageType::Rust;
    /// assert!(lang.allows_nested());
    /// ```
    pub fn allows_nested(self) -> bool {
        match self {
            {% for key, v in languages -%}
                {{key}} => {{ v.nested | default(value=false) }},
            {% endfor %}
        }
    }

    /// Returns what nested comments the language has. (Currently only D has
    /// any of this type.)
    /// ```
    /// use tokei::LanguageType;
    /// let lang = LanguageType::D;
    /// assert_eq!(lang.nested_comments(), &[("/+", "+/")]);
    /// ```
    pub fn nested_comments(self) -> &'static [(&'static str, &'static str)]
    {
        match self {
            {% for key, value in languages -%}
                {{key}} => &[
                    {%- for items in value.nested_comments | default(value=[]) -%}
                        ({% for item in items %}"{{item}}",{% endfor %}),
                    {%- endfor -%}
                ],
            {% endfor %}
        }
    }

    /// Returns the quotes of a language.
    /// ```
    /// use tokei::LanguageType;
    /// let lang = LanguageType::C;
    /// assert_eq!(lang.quotes(), &[("\"", "\"")]);
    /// ```
    pub fn quotes(self) -> &'static [(&'static str, &'static str)] {
        match self {
            {% for key, value in languages -%}
                {{key}} => &[
                    {%- for items in value.quotes | default(value=[]) -%}
                        ({% for item in items %}"{{item}}",{% endfor %}),
                    {%- endfor -%}
                ],
            {% endfor %}
        }
    }

    /// Returns the verbatim quotes of a language.
    /// ```
    /// use tokei::LanguageType;
    /// let lang = LanguageType::CSharp;
    /// assert_eq!(lang.verbatim_quotes(), &[("@\"", "\"")]);
    /// ```
    pub fn verbatim_quotes(self) -> &'static [(&'static str, &'static str)] {
        match self {
            {% for key, value in languages -%}
                {{key}} => &[
                    {%- for items in value.verbatim_quotes | default(value=[]) -%}
                        ({% for item in items %}"{{item}}",{% endfor %}),
                    {%- endfor -%}
                ],
            {% endfor %}
        }
    }

    /// Returns the doc quotes of a language.
    /// ```
    /// use tokei::LanguageType;
    /// let lang = LanguageType::Python;
    /// assert_eq!(lang.doc_quotes(), &[("\"\"\"", "\"\"\""), ("'''", "'''")]);
    /// ```
    pub fn doc_quotes(self) -> &'static [(&'static str, &'static str)] {
        match self {
            {% for key, value in languages -%}
                {{key}} => &[
                    {% for items in value.doc_quotes | default(value=[])-%}
                        ({% for item in items %}"{{item}}",{% endfor %}),
                    {%- endfor %}
                ],
            {%- endfor %}
        }
    }

    /// Returns the shebang of a language.
    /// ```
    /// use tokei::LanguageType;
    /// let lang = LanguageType::Bash;
    /// assert_eq!(lang.shebangs(), &["#!/bin/bash"]);
    /// ```
    pub fn shebangs(self) -> &'static [&'static str] {
        match self {
            {% for key, lang in languages -%}
                {{key}} => &[{% for item in lang.shebangs | default(value=[]) %}"{{item}}",{% endfor %}],
            {% endfor %}
        }
    }

    pub(crate) fn any_multi_line_comments(self) -> &'static [(&'static str, &'static str)] {
        match self {
            {% for key, value in languages -%}
                {{key}} => &[
                {%- set starting_multi_line_comments = value.multi_line_comments | default(value=[]) -%}
                {%- set starting_nested_comments = value.nested_comments | default(value=[]) -%}
                    {%- for item in starting_multi_line_comments | concat(with=starting_nested_comments) -%}
                        ("{{item.0}}", "{{item.1}}"),
                    {%- endfor -%}
                ],
            {% endfor %}
        }
    }

    pub(crate) fn any_comments(self) -> &'static [&'static str] {
        match self {
            {% for key, value in languages -%}
                {{key}} => &[
                {%- set starting_multi_line_comments = value.multi_line_comments | default(value=[]) -%}
                {%- set starting_nested_comments = value.nested_comments | default(value=[]) -%}

                    {%- for item in starting_multi_line_comments | concat(with=starting_nested_comments) -%}
                        "{{item.0}}",
                        "{{item.1}}",
                    {%- endfor -%}
                    {%- for item in value.line_comment | default(value=[]) -%}
                        "{{item}}",
                    {%- endfor -%}
                ],
            {% endfor %}
        }
    }

    /// Returns the parts of syntax that determines whether tokei can skip large
    /// parts of analysis.
    pub fn important_syntax(self) -> &'static [&'static str] {
        match self {
            {% for key, value in languages -%}
                {%- set starting_quotes = value.quotes | default(value=[]) | map(attribute="0") -%}
                {%- set starting_doc_quotes = value.doc_quotes | default(value=[]) | map(attribute="0") -%}
                {%- set starting_multi_line_comments = value.multi_line_comments | default(value=[]) | map(attribute="0") -%}
                {%- set starting_nested_comments = value.nested_comments | default(value=[]) | map(attribute="0") -%}
                {%- set important_syntax = value.important_syntax | default(value=[]) -%}

                {{key}} => &[
                    {%- for item in starting_quotes |
                                   concat(with=starting_doc_quotes) |
                                   concat(with=starting_multi_line_comments) |
                                   concat(with=starting_nested_comments) |
                                   concat(with=important_syntax) -%}
                        "{{item}}",
                    {%- endfor -%}
                    {%- for context in value.contexts | default(value=[]) -%}
                        {% if value.kind == "html" %}
                            "<{{context.tag}}",
                        {% endif %}
                    {%- endfor -%}
                ],
            {% endfor %}
        }
    }

    /// Get language from a file path. May open and read the file.
    ///
    /// ```no_run
    /// use tokei::{Config, LanguageType};
    ///
    /// let rust = LanguageType::from_path("./main.rs", &Config::default());
    ///
    /// assert_eq!(rust, Some(LanguageType::Rust));
    /// ```
    pub fn from_path<P: AsRef<Path>>(entry: P, _config: &Config)
        -> Option<Self>
    {
        let entry = entry.as_ref();

        if let Some(filename) = fsutils::get_filename(entry) {
            match &*filename {
                {% for key, value in languages -%}
                    {%- if value.filenames -%}
                        {%- for item in value.filenames -%}
                            | "{{item}}"
                        {%- endfor -%}
                            => return Some({{key}}),
                    {% endif -%}
                {%- endfor %}
                _ => ()
            }
        }

        match fsutils::get_extension(entry) {
            Some(extension) => LanguageType::from_file_extension(extension.as_str()),
            None => LanguageType::from_shebang(&entry),
        }
    }

    /// Get language from a file extension.
    ///
    /// ```no_run
    /// use tokei::LanguageType;
    ///
    /// let rust = LanguageType::from_file_extension("rs");
    ///
    /// assert_eq!(rust, Some(LanguageType::Rust));
    /// ```
    #[must_use]
    pub fn from_file_extension(extension: &str) -> Option<Self> {
        match extension {
            {% for key, value in languages -%}
                {%- if value.extensions -%}
                    {%- for item in value.extensions  %}| "{{item}}" {% endfor %}=> Some({{key}}),
                {% endif -%}
            {%- endfor %}
            extension => {
                warn!("Unknown extension: {}", extension);
                None
            },
        }
    }

    /// Get language from its MIME type if available.
    ///
    /// ```no_run
    /// use tokei::LanguageType;
    ///
    /// let lang = LanguageType::from_mime("application/javascript");
    ///
    /// assert_eq!(lang, Some(LanguageType::JavaScript));
    /// ```
    #[must_use]
    pub fn from_mime(mime: &str) -> Option<Self> {
        match mime {
            {% for key, value in languages -%}
                {%- if value.mime -%}
                    {%- for item in value.mime  %}| "{{item}}" {% endfor %}=> Some({{key}}),
                {% endif -%}
            {%- endfor %}
            _ => {
                warn!("Unknown MIME: {}", mime);
                None
            },
        }
    }

    /// Get language from a shebang. May open and read the file.
    ///
    /// ```no_run
    /// use tokei::LanguageType;
    ///
    /// let rust = LanguageType::from_shebang("./main.rs");
    ///
    /// assert_eq!(rust, Some(LanguageType::Rust));
    /// ```
    pub fn from_shebang<P: AsRef<Path>>(entry: P) -> Option<Self> {
        let file = match File::open(entry) {
            Ok(file) => file,
            _ => return None,
        };

        let mut buf = BufReader::new(file);
        let mut line = String::new();
        let _ = buf.read_line(&mut line);

        let mut words = line.split_whitespace();
        match words.next() {
            {# First match against any shebang paths, and then check if the
               language matches any found in the environment shebang path. #}
            {% for key, value in languages -%}
                {%- if value.shebangs %}
                    {%- for item in value.shebangs  %}| Some("{{item}}") {% endfor %}=> Some({{key}}),
                {% endif -%}
            {%- endfor %}

            Some("#!/usr/bin/env") => {
                if let Some(word) = words.next() {
                    match word {
                        {% for key, value in languages -%}
                            {%- if value.env -%}
                                {%- for item in value.env  %}
                                    {% if loop.index == 1 %}
                                        _ if word.starts_with("{{item}}")
                                    {% else %}
                                        || word.starts_with("{{item}}")
                                    {% endif %}
                                {% endfor %}=> Some({{key}}),
                            {% endif -%}
                        {%- endfor %}
                        env => {
                            warn!("Unknown environment: {:?}", env);
                            None
                        }
                    }
                } else {
                    None
                }
            }
            _ => None,
        }
    }
}

impl FromStr for LanguageType {
    type Err = &'static str;

    fn from_str(from: &str) -> Result<Self, Self::Err> {
        match &*from.to_lowercase() {
            {% for key, value in languages %}
                {% if value.name %}"{{value.name | lower}}"{% else %}"{{key | lower}}"{% endif %}
                => Ok({{key}}),
            {% endfor %}
            _ => Err("Language not found, please use `-l` to see all available\
                     languages."),
        }
    }
}

impl fmt::Display for LanguageType {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.name())
    }
}


impl<'a> From<LanguageType> for Cow<'a, LanguageType> {
    fn from(from: LanguageType) -> Self {
        Cow::Owned(from)
    }
}

impl<'a> From<&'a LanguageType> for Cow<'a, LanguageType> {
    fn from(from: &'a LanguageType) -> Self {
        Cow::Borrowed(from)
    }
}
