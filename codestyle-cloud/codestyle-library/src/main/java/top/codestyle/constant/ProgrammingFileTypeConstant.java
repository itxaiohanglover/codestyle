package top.codestyle.constant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 编程文件类型常量类
 *
 * @author huxc2020
 */
public class ProgrammingFileTypeConstant {

    /**
     * 允许上传的编程文件扩展名集合
     */
    public static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            // Java相关
            "java", "class", "jar",
            // JavaScript/TypeScript
            "js", "jsx", "ts", "tsx", "mjs", "cjs",
            // 前端框架
            "vue", "svelte",
            // Python
            "py", "pyw", "pyc",
            // C/C++
            "c", "cpp", "cc", "cxx", "h", "hpp", "hxx",
            // C#
            "cs", "csx",
            // Go
            "go",
            // Rust
            "rs",
            // PHP
            "php", "php3", "php4", "php5", "phtml",
            // Ruby
            "rb", "rbw",
            // Swift
            "swift",
            // Kotlin
            "kt", "kts",
            // Scala
            "scala", "sc",
            // R
            "r", "R",
            // Shell脚本
            "sh", "bash", "zsh", "fish",
            // SQL
            "sql",
            // HTML/CSS
            "html", "htm", "css", "scss", "sass", "less",
            // XML/JSON/YAML
            "xml", "json", "yaml", "yml", "toml",
            // Markdown
            "md", "markdown",
            // 配置文件
            "properties", "conf", "config", "ini",
            // Groovy
            "groovy", "gradle",
            // Objective-C
            "m", "mm",
            // Perl
            "pl", "pm",
            // Lua
            "lua",
            // Dart
            "dart",
            // Elixir
            "ex", "exs",
            // Erlang
            "erl", "hrl",
            // Haskell
            "hs", "lhs",
            // Clojure
            "clj", "cljs", "cljc",
            // F#
            "fs", "fsi", "fsx",
            // Visual Basic
            "vb", "vbs",
            // Fortran
            "f", "f90", "f95",
            // Pascal
            "pas", "pp",
            // Assembly
            "asm", "s",
            // Makefile
            "mk", "make"
    ));

    /**
     * 检查文件扩展名是否为编程文件
     *
     * @param fileName 文件名
     * @return 是否为编程文件
     */
    public static boolean isProgrammingFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            // Makefile等无扩展名的文件
            String lowerFileName = fileName.toLowerCase();
            return lowerFileName.equals("makefile") ||
                   lowerFileName.equals("dockerfile") ||
                   lowerFileName.equals("jenkinsfile");
        }

        String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    private ProgrammingFileTypeConstant() {
        // 工具类，禁止实例化
    }
}

