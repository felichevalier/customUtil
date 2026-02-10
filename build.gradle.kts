plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"

}

group = "com.custom"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation(kotlin("test"))
    implementation("cloud.commandframework:cloud-paper:1.8.4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks {
    // Shadow JARのタスク設定
    shadowJar {
        archiveFileName.set("${project.name}-${version}.jar")
    }

    // 通常のJARファイルの生成を無効化
    jar {
        enabled = false
    }
}

tasks.register<Copy>("devServer") {
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar.map { it.archiveFile })
    into("devserver/plugins")
}