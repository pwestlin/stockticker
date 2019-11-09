plugins {
    application
    kotlin("jvm")
}

application {
    mainClassName = "cli.Main"
}

dependencies {
    implementation(project(":model"))
    implementation(kotlin("stdlib"))
}