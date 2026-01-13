plugins {
    glass(JAVA)
    glass(PUBLISHING)
    glass(SIGNING)
    spotless(GRADLE)
    spotless(JAVA)
}

group = "top.nustar.nustarparty"
version = "1.1.0"

allprojects {
    if (!project.buildFile.exists()) {
        return@allprojects
    }

    apply {
        glass(JAVA)
        glass(PUBLISHING)
        glass(SIGNING)
        spotless(GRADLE)
        spotless(JAVA)
    }

    group = rootProject.group
    version = rootProject.version

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
            vendor.set(JvmVendorSpec.AZUL)
        }
    }

    glass {
        release.set(8)

        application {
            sugar {
                enabled.set(true)
            }
        }

        withCopyright()
        withMavenPom()

        withSourcesJar()
        withJavadocJar()

        withInternal()
        withShadow()

        withJUnitTest()
    }

    repositories {
        mavenLocal()
        aliyun()
        sonatype()
        sonatype(SNAPSHOT)
        maven {
            name = "spigotmc-repo"
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }
        maven {
            name = "placeholder-api"
            url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        }
        maven {
            name = "paper"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            name = "nustar-repo"
            url = uri("https://maven.nustar.top/repository/nustar-public/")
        }
        mavenCentral()
    }

    dependencies {
        if (project.name.contains("hook")) {
            implementation(rootProject.project(":${rootProject.name}-party-api"))
        }
        if (project.name.contains("core")) {
            implementation(rootProject.project(":${rootProject.name}-party-api"))
            internal(rootProject.libs.bstats)
        }
        compileOnly(rootProject.libs.nustar.gui)
        @Suppress("VulnerableLibrariesLocal", "RedundantSuppression")
        compileOnly(rootProject.libs.spigot.api)
        compileOnly(rootProject.libs.placeholderapi)
        @Suppress("VulnerableLibrariesLocal", "RedundantSuppression")
        compileOnly(rootProject.libs.kotlin.stdlib)
        compileOnly(rootProject.libs.minecraft.next.spigot)
        compileOnly(rootProject.libs.nustar.corebridge)
        compileOnly(rootProject.libs.mythicdungeons)
        compileOnly(rootProject.libs.dungeonplus)
        compileOnly(rootProject.libs.monsterapi)
        compileOnly(fileTree(File(rootProject.projectDir, "libraries")))
        compileOnly(rootProject.libs.lombok)
        compileOnly(rootProject.libs.lombok)
        annotationProcessor(rootProject.libs.lombok)
        testCompileOnly(rootProject.libs.lombok)
        testAnnotationProcessor(rootProject.libs.lombok)
    }
    tasks.processResources {
        val props =
            mapOf(
                "name" to rootProject.name,
                "version" to rootProject.version,
            )
        filesMatching(listOf("plugin.yml")) {
            expand(props)
        }
    }

    publishing {
        repositories {
            project(project)
            maven {
                name = "nustar-releases"
                url = uri("https://maven.nustar.top/repository/nustar-releases/")
                properties(project).login()
            }
        }
    }
}

dependencies {
    implementation(rootProject.libs.nustar.gui)
    subprojects {
        shadow(rootProject.project(name))
    }
}
