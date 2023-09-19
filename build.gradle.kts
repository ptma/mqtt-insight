@file:Suppress("UNCHECKED_CAST")

import groovy.lang.Closure
import io.github.fvarrui.javapackager.gradle.PackagePluginExtension
import io.github.fvarrui.javapackager.gradle.PackageTask
import io.github.fvarrui.javapackager.model.*
import io.github.fvarrui.javapackager.model.Platform
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.internal.os.OperatingSystem
import java.nio.charset.Charset

plugins {
    `java-library`
}

buildscript {
    repositories {
        maven("https://maven.aliyun.com/repository/public/")
        mavenLocal()
        mavenCentral()
        dependencies {
            // ********* package with gradle 7.6.2 *********
            classpath("io.github.fvarrui:javapackager:1.6.7")
        }
    }
}

plugins.apply("io.github.fvarrui.javapackager.plugin")

version = "1.0.0"

val applicationName: String = "MqttInsight"
val organization: String = "ptma@163.com"
val copyright: String = "copyright 2023 ptma@163.com"
val supportUrl: String = "https://github.com/ptma/mqtt-insight"


val flatlafVersion = "3.2.1"
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
        exclude("asm")
    }
    implementation("com.miglayout:miglayout-swing:11.1")

    implementation("com.fifesoft:rsyntaxtextarea:3.3.4")
    implementation(files("libs/swing-toast-notifications-1.0.1.jar"))

    implementation("cn.hutool:hutool-json:5.8.20")

    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")

    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.8")

    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")

    if (OperatingSystem.current().isMacOsX()) {
        implementation("com.caoccao.javet:javet-macos:2.2.2") // Mac OS (x86_64 and arm64)
    } else {
        implementation("com.caoccao.javet:javet:2.2.2") // Linux and Windows (x86_64)
    }
}
repositories {
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

tasks.processResources {
    updateVersion()
}


tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes("Main-Class" to "com.mqttinsight.MqttInsightApplication")
        attributes("Implementation-Vendor" to "https://github.com/ptma/mqtt-insight")
        attributes("Implementation-Copyright" to "MqttInsight")
        attributes("Implementation-Version" to project.version)
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
}



tasks.register<PackageTask>("packageForWindows") {

    val innoSetupLanguageMap = LinkedHashMap<String, String>()
    innoSetupLanguageMap["Chinese"] = "compiler:Languages\\ChineseSimplified.isl"
    innoSetupLanguageMap["English"] = "compiler:Default.isl"

    description = "package For Windows"

    organizationName = organization
    organizationUrl = supportUrl

    platform = Platform.windows
    isCreateZipball = false
    winConfig(closureOf<WindowsConfig> {
        icoFile = getIconFile("MqttInsight.ico")
        headerType = HeaderType.gui
        originalFilename = applicationName
        copyright = copyright
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
    description = "package For Linux"
    platform = Platform.linux

    organizationName = organization
    organizationUrl = supportUrl

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
    description = "package For Mac"
    platform = Platform.mac

    organizationName = organization
    organizationUrl = supportUrl

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
    description = "package For Mac"
    platform = Platform.mac

    organizationName = organization
    organizationUrl = supportUrl

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

fun updateVersion() {
    val jsonFile = File(projectDir.absolutePath + File.separator + "assets" + File.separator + "version.json")
    jsonFile.writeText("{\"version\": \"${version}\"}", Charset.forName("utf-8"))
}
