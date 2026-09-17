plugins {
    `java-library`
    id("net.neoforged.moddev") version "2.0.140"
}

group = "com.melon"
version = providers.gradleProperty("mod_version").get()
base.archivesName.set("sable-deployer-rotation-fix")

repositories {
    mavenCentral()

    flatDir {
        dirs("libs")
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "ParchmentMC"
                url = uri("https://maven.parchmentmc.org/")
            }
        }
        filter {
            includeGroup("org.parchmentmc.data")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "BlameJared"
                url = uri("https://maven.blamejared.com/")
            }
        }
        filter {
            includeGroup("mezz.jei")
            includeGroup("foundry.veil")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Registrate"
                url = uri("https://maven.ithundxr.dev/snapshots/")
            }
        }
        filter {
            includeGroup("com.tterrag.registrate")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Create"
                url = uri("https://maven.createmod.net/")
            }
        }
        filter {
            includeGroup("com.simibubi.create")
            includeGroup("net.createmod.ponder")
            includeGroup("dev.engine-room.flywheel")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "RyanHCode"
                url = uri("https://maven.ryanhcode.dev/releases/")
            }
        }
        filter {
            includeGroup("dev.ryanhcode.sable-companion")
            includeGroup("dev.ryanhcode.sable")
            includeGroup("dev.simulated_team.simulated")
            includeGroup("dev.eriksonn.aeronautics")
            includeGroup("dev.ryanhcode.offroad")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Curios"
                url = uri("https://maven.theillusivec4.top/")
            }
        }
        filter {
            includeGroup("top.theillusivec4.curios")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "CCTweaked"
                url = uri("https://maven.squiddev.cc/")
            }
        }
        filter {
            includeGroup("cc.tweaked")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "KotlinForForge"
                url = uri("https://thedarkcolour.github.io/KotlinForForge/")
            }
        }
        filter {
            includeGroup("thedarkcolour")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "RealRobotix"
                url = uri("https://maven.realrobotix.me/master/")
            }
        }
        filter {
            includeGroup("com.copycatsplus")
            includeGroup("com.rbasamoyai")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "LowDragLib"
                url = uri("https://maven.firstdarkdev.xyz/snapshots/")
            }
        }
        filter {
            includeGroup("com.lowdragmc.ldlib2")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "CurseMaven"
                url = uri("https://cursemaven.com/")
            }
        }
        filter {
            includeGroup("curse.maven")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven/")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

neoForge {
    version = providers.gradleProperty("neoforge_version").get()
    runs {
        create("client") { client() }
        create("server") { server(); programArgument("--nogui") }
    }
    mods {
        create("sable_deployer_rotation_fix") { sourceSet(sourceSets.main.get()) }
    }
}
dependencies {
    implementation("com.simibubi.create:create-1.21.1:${property("create_version")}") { isTransitive = false }
    implementation("maven.modrinth:sable:${property("sable_version_id")}") { isTransitive = false }
    // Needed on compileClasspath for SubLevel's public Pose and companion types.
    compileOnly("dev.ryanhcode.sable-companion:sable-companion-common-1.21.1:1.6.0")
    implementation("net.createmod.ponder:ponder-neoforge:1.0.82+mc1.21.1")
    compileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-1.21.1:1.0.6")
    runtimeOnly("dev.engine-room.flywheel:flywheel-neoforge-1.21.1:1.0.6")
    implementation("com.tterrag.registrate:Registrate:MC1.21-1.3.0+67")
    // Optional, user-supplied matching test mods; never embedded in this jar.
    runtimeOnly(fileTree("runtime-mods") { include("*.jar") })
    // Installed in runClient/runServer, but not exposed to AeroSkylands source code.
    runtimeOnly("dev.simulated_team.simulated:simulated-neoforge-1.21.1:1.3.0") {
        isTransitive = false
    }
    runtimeOnly("dev.eriksonn.aeronautics:aeronautics-neoforge-1.21.1:1.3.0") {
        isTransitive = false
    }

    runtimeOnly("mezz.jei:jei-1.21.1-neoforge:19.27.0.346") {
        isTransitive = false
    }
    runtimeOnly("top.theillusivec4.curios:curios-neoforge:9.2.2+1.21.1")
    // Aeronautics/Sable integration-test addons.
    runtimeOnly("curse.maven:create-aeronautics-transmission-linkage-1548056:8264158")
    runtimeOnly("curse.maven:drive-by-wire-with-sable-1520378:8247104")
    runtimeOnly("curse.maven:synaxis-1526348:8412557")
    runtimeOnly("curse.maven:sable-schematic-tool-1526951:8247085")
    runtimeOnly("cc.tweaked:cc-tweaked-1.21.1-forge:1.120.0")
    runtimeOnly("com.lowdragmc.ldlib2:ldlib2-neoforge-1.21.1:2.2.27:all") {
        isTransitive = false
    }
}
tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
}
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
