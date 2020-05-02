import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ProjectDefaultsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            configurePlugins()
            configureBuild()
            configureDependencies()
            configureTests()
        }
    }
}

private fun Project.configurePlugins() {
    plugins.apply("java-library")
    plugins.apply("org.jetbrains.kotlin.jvm")
}

private fun Project.configureBuild() {
    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs += listOf(
                    "-progressive",
                    "-Xjsr305=strict"
                )
                javaParameters = true
                jvmTarget = "11"
            }
        }
    }
}

private fun Project.configureDependencies() {
    repositories {
        mavenCentral()
    }

    dependencies {
        // Apply platform dependencies to constrain versions across projects

        "implementation"(platform("com.amazonaws:aws-java-sdk-bom:1.11.769"))
        "implementation"(platform("com.fasterxml.jackson:jackson-bom:2.10.3"))
        "implementation"(platform("org.apache.logging.log4j:log4j-bom:2.13.2"))

        // Common dependencies for all projects

        "implementation"("org.apache.logging.log4j:log4j-api")
        "implementation"("org.apache.logging.log4j:log4j-core")
        "implementation"("org.apache.logging.log4j:log4j-slf4j18-impl")
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        "testImplementation"(platform("org.junit:junit-bom:5.6.2"))

        "testImplementation"("io.mockk:mockk:1.10.0")
        "testImplementation"("org.assertj:assertj-core:3.12.2")
        "testImplementation"("org.junit.jupiter:junit-jupiter-api")

        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine")
    }
}

private fun Project.configureTests() {
    tasks {
        withType(Test::class) {
            useJUnitPlatform()

            testLogging {
                events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
            }
        }
    }
}