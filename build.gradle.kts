import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "me.sudodios.codewalker"
version = "cw-candle"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

fun getLibExt () : String = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> "dll"
    Os.isFamily(Os.FAMILY_UNIX) -> "so"
    Os.isFamily(Os.FAMILY_MAC) -> "dylib"
    else -> ""
}

fun getLibName () : String = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> "core_code_walker"
    Os.isFamily(Os.FAMILY_UNIX) || Os.isFamily(Os.FAMILY_MAC)  -> "libcore_code_walker"
    else -> ""
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
    dependsOn("buildCore")
}

tasks.register("buildCore") {
    exec {
        commandLine("cargo","build","--manifest-path=src/main/core/Cargo.toml","--release")
    }
    doLast {
        copy {
            from("src/main/core/target/release/${getLibName()}.${getLibExt()}")
            into("src/main/resources")
        }
    }
}

dependencies {
    //implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(compose.desktop.currentOs)
    api(compose.foundation)
    api(compose.animation)

    implementation("org.jetbrains.compose.material3:material3-desktop:1.5.11")
    implementation("moe.tlaster:precompose:1.5.10")
    implementation("dev.icerock.moko:mvvm-livedata-compose:0.16.1")
    implementation("com.google.code.gson:gson:2.10.1")
}

compose.desktop {
    application {
        mainClass = "me.sudodios.codewalker.CodeWalkerKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "CodeWalker"
            packageVersion = "1.0.0"
            val iconsRoot = project.file("src/main/resources")
            linux {
                iconFile.set(iconsRoot.resolve("icons/app-icon.png"))
            }
            windows {
                iconFile.set(iconsRoot.resolve("icons/app-icon.ico"))
            }
            buildTypes.release.proguard {
                obfuscate.set(true)
                version.set("7.4.1")
                configurationFiles.from(project.file("rules.pro").absolutePath)
            }
        }
    }
}
