plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":model"))

    implementation("org.springframework.fu:spring-fu-kofu:0.2.2.BUILD-SNAPSHOT")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    //implementation("org.springframework.data:spring-data-r2dbc:1.0.0.RC1")
    //implementation("io.r2dbc:r2dbc-h2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.0")

    //developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.mockk:mockk:1.9.3")
}

dependencyManagement {
    imports {
        mavenBom("io.r2dbc:r2dbc-bom:Arabba-RC2")
    }
}
