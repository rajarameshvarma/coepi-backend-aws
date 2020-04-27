plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("project-defaults-plugin") {
            id = "project-defaults"
            implementationClass = "ProjectDefaultsPlugin"
            description = "Applies common project settings"
        }
    }
}

repositories {
    jcenter()
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
}
