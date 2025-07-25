@file:Suppress("UNCHECKED_CAST")

import groovy.json.JsonSlurper
import groovy.lang.Closure
import io.github.fvarrui.javapackager.gradle.PackagePluginExtension
import io.github.fvarrui.javapackager.gradle.PackageTask
import io.github.fvarrui.javapackager.model.*
import io.github.fvarrui.javapackager.model.Platform
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.internal.os.OperatingSystem

plugins {
    `java-library`
}

buildscript {
    repositories {
        maven(url = "https://maven.aliyun.com/repository/public/")
        mavenLocal()
        mavenCentral()
        dependencies {
            // ********* package with gradle 7.6.2 *********
            // @see https://githubfast.com/fvarrui/JavaPackager/issues/315
            // classpath("io.github.fvarrui:javapackager:1.6.7")
            classpath("io.github.fvarrui:javapackager:1.7.6")
        }
    }
}

plugins.apply("io.github.fvarrui.javapackager.plugin")

val versionConfig = "${rootProject.projectDir.path}/src/main/resources/version.json"
val versionJson = JsonSlurper().parse(File(versionConfig)) as Map<String, String>
val appliactionVersion = versionJson.get("version")
val applicationName: String = "MqttInsight"
val organization: String = "ptma@163.com"
val copyrightVal: String = "Copyright (C) ptma@163.com"
val supportUrl: String = "https://github.com/ptma/mqtt-insight"

val flatlafVersion = "3.4"
val javetVersion = "3.1.8"
val fatJar = false

val requireModules = listOf(
    "java.base",
    "java.desktop",
    "java.prefs",
    "java.logging",
    "java.naming",
    "java.sql",
    "java.scripting",
    "java.xml",
    "jdk.dynalink",
    "jdk.unsupported",
    "jdk.management"
)

if (JavaVersion.current() < JavaVersion.VERSION_17)
    throw RuntimeException("compile required Java ${JavaVersion.VERSION_17}, current Java ${JavaVersion.current()}")

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    testCompileOnly("org.projectlombok:lombok:1.18.20")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.20")

    implementation("com.formdev:flatlaf:${flatlafVersion}")
    implementation("com.formdev:flatlaf-swingx:${flatlafVersion}")
    implementation("com.formdev:flatlaf-extras:${flatlafVersion}")
    implementation("com.formdev:flatlaf-intellij-themes:${flatlafVersion}")
    implementation("com.formdev:flatlaf-fonts-jetbrains-mono:2.242")
    implementation("at.swimmesberger:swingx-core:1.6.8")

    implementation("com.jgoodies:jgoodies-forms:1.9.0")
    implementation("com.intellij:forms_rt:7.0.3") {
        exclude(group = "asm", module = "asm-commons")
    }
    implementation("com.miglayout:miglayout-swing:11.3")

    implementation("com.fifesoft:rsyntaxtextarea:3.3.4")
    implementation(files("libs/swing-toast-notifications-1.0.1.jar"))

    implementation("cn.hutool:hutool-core:5.8.24")

    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")

    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("org.slf4j:jul-to-slf4j:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.8")

    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")

    if (OperatingSystem.current().isMacOsX) {
        implementation("com.caoccao.javet:javet-macos:${javetVersion}") // Mac OS (x86_64 and arm64)
    } else {
        implementation("com.caoccao.javet:javet:${javetVersion}") // Linux and Windows (x86_64)
    }
    implementation("org.knowm.xchart:xchart:3.8.6") {
        exclude(group = "de.rototor.pdfbox", module = "graphics2d")
        exclude(group = "com.madgag", module = "animated-gif-lib")
    }
    implementation("com.jayway.jsonpath:json-path:2.8.0") {
        exclude(group = "net.minidev", module = "json-smart")
    }

    implementation("commons-codec:commons-codec:1.15")
    implementation("com.google.protobuf:protobuf-java:3.25.1")
    implementation("org.msgpack:jackson-dataformat-msgpack:0.9.6")
    implementation("org.apache.avro:avro:1.11.3") {
        exclude(module = "commons-compress")
    }
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-avro:2.15.2") {
        exclude(module = "commons-compress")
    }
    implementation("com.caucho:hessian:4.0.66")
    implementation("com.esotericsoftware:kryo:5.5.0")
}

repositories {
    maven(url = "https://maven.aliyun.com/repository/public/")
    mavenLocal()
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
    testLogging.exceptionFormat = TestExceptionFormat.FULL
}

tasks.compileJava {
    sourceCompatibility = "17"
    targetCompatibility = "17"
    options.encoding = "UTF-8"
    options.isDeprecation = false
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes("Main-Class" to "com.mqttinsight.MqttInsightApplication")
        attributes("Implementation-Vendor" to "https://github.com/ptma/mqtt-insight")
        attributes("Implementation-Copyright" to copyrightVal)
        attributes("Implementation-Version" to appliactionVersion)
        attributes("Multi-Release" to "true")
    }

    exclude("module-info.class")
    exclude("META-INF/versions/*/module-info.class")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.LIST")
    exclude("META-INF/*.factories")

    if (fatJar) {
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.endsWith("jar") }
                .map {
                    zipTree(it).matching {
                        exclude("META-INF/LICENSE")
                    }
                }
        })
    }

    from("${rootDir}/LICENSE") {
        into("META-INF")
    }
}

configure<PackagePluginExtension> {
    mainClass("com.mqttinsight.MqttInsightApplication")
    packagingJdk(File(System.getProperty("java.home")))
    bundleJre(true)
    customizedJre(true)
    modules(requireModules)
    jreDirectoryName("jre")
    vmArgs(
        listOf(
            "-Xms256M",
            "-Xmx2048M"
        )
    )
}

var taskPlatform = Platform.windows
var taskPlatform_M1 = false
tasks.register<Copy>("extractJavet") {
    delete(layout.buildDirectory.dir("javet"))

    if (taskPlatform == Platform.mac) {
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.equals("javet-macos-${javetVersion}.jar") }
                .map {
                    zipTree(it).matching {
                        if (taskPlatform_M1) {
                            exclude("**/*x86_64.*.dylib")
                        } else {
                            exclude("**/*arm64.*.dylib")
                        }
                    }
                }
        })
    } else {
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.equals("javet-${javetVersion}.jar") }
                .map {
                    zipTree(it).matching {
                        if (taskPlatform == Platform.windows) {
                            exclude("**/*.so")
                        } else if (taskPlatform == Platform.linux) {
                            exclude("**/*.dll")
                        }
                    }
                }
        })
    }
    into(layout.buildDirectory.dir("javet"))
}

tasks.register<Jar>("repackJavet") {
    if (taskPlatform == Platform.mac) {
        archiveBaseName.set("javet-macos-${javetVersion}")
    } else {
        archiveBaseName.set("javet-${javetVersion}")
    }
    from(layout.buildDirectory.dir("javet"))
    manifest.attributes["Automatic-Module-Name"] = "com.caoccao.javet"

    dependsOn("extractJavet")
}

tasks.register<Copy>("replaceJavet") {
    if (taskPlatform == Platform.mac) {
        from(layout.buildDirectory.file("libs/javet-macos-${javetVersion}.jar"))
        into(layout.buildDirectory.dir("MqttInsight.app/Contents/Resources/Java/libs"))
    } else {
        from(layout.buildDirectory.file("libs/javet-${javetVersion}.jar"))
        into(layout.buildDirectory.dir("MqttInsight/libs"))
    }

    dependsOn("repackJavet")
}

tasks.register<Copy>("copyLibs") {
    doLast {
        val taskExtract = tasks.findByPath("extractJavet")
        taskExtract?.actions?.forEach { action ->
            action.execute(taskExtract)
        }
        val taskRepack = tasks.findByPath("repackJavet")
        taskRepack?.actions?.forEach { action ->
            action.execute(taskRepack)
        }
        val taskReplace = tasks.findByPath("replaceJavet")
        taskReplace?.actions?.forEach { action ->
            action.execute(taskReplace)
        }
    }
}

tasks.register<PackageTask>("packageForWindows") {
    taskPlatform = Platform.windows

    val innoSetupLanguageMap = LinkedHashMap<String, String>()
    innoSetupLanguageMap["Chinese"] = "compiler:Languages\\ChineseSimplified.isl"
    innoSetupLanguageMap["English"] = "compiler:Default.isl"

    description = "package For Windows"

    organizationName = organization
    organizationUrl = supportUrl
    version = appliactionVersion;

    platform = Platform.windows
    isCreateZipball = false
    winConfig(closureOf<WindowsConfig> {
        icoFile = getIconFile("MqttInsight.ico")
        headerType = HeaderType.gui
        originalFilename = applicationName
        copyright = copyrightVal
        productName = applicationName
        productVersion = version
        fileVersion = version
        isGenerateSetup = false
        setupLanguages = innoSetupLanguageMap
        isCreateZipball = true
        isGenerateMsi = false
        isGenerateMsm = false
        msiUpgradeCode = version
        isDisableDirPage = false
        isDisableFinishedPage = false
        isDisableWelcomePage = false
    } as Closure<WindowsConfig>)
    dependsOn(tasks.build)
}

tasks.register<PackageTask>("packageForLinux") {
    taskPlatform = Platform.linux

    description = "package For Linux"
    platform = Platform.linux

    organizationName = organization
    organizationUrl = supportUrl
    version = appliactionVersion;

    linuxConfig(
        closureOf<LinuxConfig> {
            pngFile = getIconFile("MqttInsight.png")
            isGenerateDeb = true
            isGenerateRpm = true
            isCreateTarball = true
            isGenerateInstaller = true
            categories = listOf("Office")
        } as Closure<LinuxConfig>
    )
    dependsOn(tasks.build)
}

tasks.register<PackageTask>("packageForMac_M1") {
    taskPlatform = Platform.mac
    taskPlatform_M1 = true

    description = "package For Mac M1"
    platform = Platform.mac

    organizationName = organization
    organizationUrl = supportUrl
    version = appliactionVersion;

    macConfig(
        closureOf<MacConfig> {
            icnsFile = getIconFile("MqttInsight.icns")
            isGenerateDmg = true
            macStartup = MacStartup.ARM64
        } as Closure<MacConfig>
    )
    dependsOn(tasks.build)
}

tasks.register<PackageTask>("packageForMac") {
    taskPlatform = Platform.mac
    taskPlatform_M1 = true

    description = "package For Mac"
    platform = Platform.mac

    organizationName = organization
    organizationUrl = supportUrl
    version = appliactionVersion;

    macConfig(
        closureOf<MacConfig> {
            icnsFile = getIconFile("MqttInsight.icns")
            isGenerateDmg = true
            macStartup = MacStartup.X86_64
        } as Closure<MacConfig>
    )
    dependsOn(tasks.build)
}

fun getIconFile(fileName: String): File {
    return File(projectDir.absolutePath + File.separator + "assets" + File.separator + fileName)
}

