plugins {
    id("application")
    id("project-defaults")

    id("com.google.cloud.tools.jib") version "2.2.0"
}

dependencies {
    implementation(project(":core"))

    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("io.ktor:ktor-server-netty")
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

jib {
    from {
        image = "adoptopenjdk:11-jdk-hotspot"
    }

    to {
        image = (property("jib.to.image") as String?) ?: "coepi-cloud-api"
    }

    container {
        mainClass = "io.ktor.server.netty.EngineMain"
    }
}