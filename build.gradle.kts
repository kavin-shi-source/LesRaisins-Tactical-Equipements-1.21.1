plugins { 
    alias(libs.plugins.moddev) 
} 
 
val mod_id = providers.gradleProperty("mod_id").get()
version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("mod_group_id").get()

repositories { 
    mavenLocal() 
    mavenCentral() 
    maven("https://jitpack.io") { 
        content { 
            includeGroup("com.github.rtyley") 
            includeGroup("com.github.FiguraMC.luaj") 
        } 
    } 
    maven("https://maven.shedaniel.me") 
    maven("https://maven.kosmx.dev") 
    maven("https://maven.blamejared.com") 
    maven("https://maven.architectury.dev") { 
        content { 
            includeGroup("dev.architectury") 
        } 
    } 
    maven("https://maven.latvian.dev/releases") { 
        content { 
            includeGroup("dev.latvian.mods") 
            includeGroup("dev.latvian.apps") 
        } 
    } 
    exclusiveContent { 
        forRepository { 
            maven("https://api.modrinth.com/maven") { 
                name = "Modrinth" 
            } 
        } 
        filter { 
            includeGroup("maven.modrinth") 
        } 
    } 
    exclusiveContent { 
        forRepository { 
            maven("https://cursemaven.com") { 
                name = "CurseForge" 
            } 
        } 
        filter { 
            includeGroup("curse.maven") 
        } 
    } 
    flatDir { 
        dir("libs") 
    } 
} 
 
neoForge { 
    version = libs.versions.neoforge.get() 
    parchment { 
        mappingsVersion = libs.versions.parchment.get() 
        minecraftVersion = libs.versions.minecraft.get() 
    } 
    validateAccessTransformers = true 
 
    // Access Transformer 
    // accessTransformers.add(file("src/main/resources/META-INF/accesstransformer.cfg")) 
 
    runs { 
        configureEach { 
            systemProperty("forge.logging.console.level", "debug") 
             
            // Add runtime dependencies for runs 
            dependencies { 
                // additionalRuntimeClasspathConfiguration(libs.org.apache.commons.math3) 
                // additionalRuntimeClasspathConfiguration(libs.luaj.core) 
                // additionalRuntimeClasspathConfiguration(libs.luaj.jse) { 
                //    exclude(group = "org.apache.bcel", module = "bcel") 
                // } 
                // additionalRuntimeClasspathConfiguration(libs.org.apache.bcel) 
            } 
        } 
 
        create("client") { 
            client() 
            gameDirectory = file("run/client") 
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id) 
        } 
        create("server") { 
            server() 
            gameDirectory = file("run/server") 
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id) 
        } 
        create("data") { 
            data() 
            programArguments.addAll( 
                "--mod", mod_id, 
                "--all", 
                "--output", file("src/generated/resources/").absolutePath, 
                "--existing", file("src/main/resources/").absolutePath 
            ) 
        } 
    } 
 
    mods { 
        create(mod_id) { 
            sourceSet(sourceSets["main"]) 
        } 
    }

    unitTest {
        enable()
        testedMod = mods.named(mod_id).get()
    } 
} 
 
sourceSets["main"].resources.srcDir("src/generated/resources") 
 
dependencies { 

    implementation("curse.maven:tacz-1-21-1-1353462:7374584") 

    compileOnly(libs.org.apache.commons.math3) 
     
    compileOnly(libs.luaj.core) 
    compileOnly(libs.luaj.jse) 

    compileOnly(libs.org.apache.bcel) 
    compileOnly(libs.cloth.config) 
    compileOnly(libs.player.animation.lib) 

    compileOnly(libs.jei.common.api) 
    compileOnly(libs.jei.neoforge.api) 
    runtimeOnly(libs.jei.neoforge) 

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2") 
    testImplementation("org.mockito:mockito-core:5.5.0") 
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0") 

} 
 
java { 
    toolchain.languageVersion = JavaLanguageVersion.of(21) 
} 
 
tasks.withType<JavaCompile> { 
    options.encoding = "UTF-8" 
    options.release.set(21) 
}

tasks.withType<ProcessResources>().configureEach {
    val replaceProperties = mapOf(
            "minecraft_version" to providers.gradleProperty("minecraft_version").get(),
            "minecraft_version_range" to providers.gradleProperty("minecraft_version_range").get(),
            "neoforge_version" to providers.gradleProperty("neoforge_version").get(),
            "neoforge_version_range" to providers.gradleProperty("neoforge_version_range").get(),
            "loader_version_range" to providers.gradleProperty("loader_version_range").get(),
            "mod_id" to mod_id,
            "mod_name" to providers.gradleProperty("mod_name").get(),
            "mod_license" to providers.gradleProperty("mod_license").get(),
            "mod_version" to version,
            "mod_authors" to providers.gradleProperty("mod_authors").get(),
            "mod_description" to providers.gradleProperty("mod_description").get()
    )
    inputs.properties(replaceProperties)

    filesMatching("**/neoforge.mods.toml") {
        expand(replaceProperties)
    }
}

tasks.test {
    useJUnitPlatform()
}