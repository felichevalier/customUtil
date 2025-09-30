plugins {
    kotlin("jvm") version "1.9.23"
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
    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
    testImplementation(kotlin("test"))
    implementation("cloud.commandframework:cloud-paper:1.8.4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}