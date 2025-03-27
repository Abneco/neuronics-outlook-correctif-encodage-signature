import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    kotlin("jvm") version "2.1.10"
    application
}

group = "be.neuronics"
version = "2025-1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.20")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.24.0")
    implementation("org.apache.logging.log4j:log4j-api:2.24.0")
    implementation("org.apache.logging.log4j:log4j-core:2.24.0")
    implementation("org.fusesource.jansi:jansi:2.4.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("be.neuronics.correctif_encodage_signature_outlook.Main")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    isZip64 = true
}




sourceSets {
    create("java8") {
        kotlin.srcDirs("src/main/kotlin")
        resources.srcDirs("src/main/resources")
        compileClasspath += configurations.runtimeClasspath.get()
        runtimeClasspath += output + compileClasspath
    }
}

// Configuration explicite Kotlin JVM 1.8
tasks.named<KotlinCompile>("compileJava8Kotlin") {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
    destinationDirectory.set(layout.buildDirectory.dir("classes/kotlin/java8"))
}

// Configuration explicite Java Compile (Javac) JVM 1.8 :
tasks.named<JavaCompile>("compileJava8Java") {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.release.set(8)

    // Toolchain explicite Java 8
    javaCompiler.set(
        javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    )
}

// Génère explicitement le JAR Java 8
val jarJava8 by tasks.registering(Jar::class) {
    dependsOn("compileJava8Kotlin", "compileJava8Java")
    archiveClassifier.set("java8")

    from(sourceSets["java8"].output)
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    isZip64 = true
}

// Génère le jar Java8 lors du build global
tasks.build {
    dependsOn(jarJava8)
}
