import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.*

plugins {
    kotlin("plugin.lombok") version "2.1.20"
    id("io.freefair.lombok") version "8.10"
    id("java-library")
    id("java")
    id("org.jetbrains.dokka") version "1.9.20" // 请根据需要调整版本号
    kotlin("jvm") version "2.1.20"
    alias(libs.plugins.shadow)
}

group = "me.huanmeng"
version = /*UUID.randomUUID().toString().substring(0,4);*/"4.4.9"
repositories {
    maven("https://maven.cleanroommc.com")
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()
    maven("https://repo.crazycrew.us/releases")
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.inventivetalent.org/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.panda-lang.org/releases")
}
tasks.named<ShadowJar>("shadowJar") {
    archiveFileName.set("ThePitUltimate-$version.jar")
    relocate("pku.yim.license", "net.mizukilab.pit.license")
    relocate("panda","net.mizukilab.pit.libs")
    relocate("dev.rollczi","net.mizukilab.pit.libs")
    relocate("cn.hutool","net.mizukilab.pit.libs")
    relocate("net.kyori","net.mizukilab.pit.libs")
    relocate("net.jodah","net.mizukilab.pit.libs")
    relocate("net.jitse","net.mizukilab.pit.libs")
    relocate("xyz.upperlevel.spigot","net.mizukilab.pit.libs")
    exclude("kotlin/**","junit/**", "org/junit/**")
    from("build/tmp/processed-resources")
    mergeServiceFiles()
}

dependencies {
    compileOnly(fileTree(mapOf("dir" to "../libs", "include" to listOf("*.jar"))))
    implementation(fileTree(mapOf("dir" to "../packLib", "include" to listOf("*.jar"))))
    api(libs.reflectionhelper)
    api(libs.hutool.crypto)
    api(libs.book)
    api(libs.slf4j)
    api(libs.litecommands)
    api(libs.adventure.bukkit)
    api(libs.kotlin)
    compileOnly("com.caoccao.javet:javet:3.1.4") // Linux and Windows (x86_64)
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    //we uses the local library from the paper 1.8.8 to get more efficient because
    //
    //    public net.minecraft.server.v1_8_R3.ItemStack handle; // Paper - public
    //
    //compileOnly(libs.spigot.get8())

    implementation("zone.rong:imaginebreaker:2.1")
    compileOnly(libs.protocollib)
    compileOnly(libs.luckperms)
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.8")
    compileOnly(libs.papi)
    compileOnly(libs.narshorn)
    compileOnly(libs.protocollib)
    compileOnly(libs.jedis)// https://mvnrepository.com/artifact/org.mongojack/mongojack
    compileOnly("org.mongojack:mongojack:5.0.1")
    compileOnly("org.mongodb:mongodb-driver-sync:5.2.0")

    compileOnly(libs.fastutil)

    compileOnly("us.crazycrew.crazycrates:api:0.7")
    compileOnly(libs.spigot.get8())
    compileOnly(libs.luckperms)
    compileOnly(libs.playerpoints)
    compileOnly(libs.decentholograms)
    compileOnly(libs.adventure.bukkit)
}
kotlin {
    jvmToolchain(17)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    archiveClassifier.set("javadoc") // 可视作替代 javadocJar
    from(tasks.dokkaHtml)
}
tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    source = sourceSets["main"].allJava
    classpath = files()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
