import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.2.2"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.helpch.at/releases")
    maven("https://repo.tcoded.com/releases")
    maven("https://jitpack.io")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.nexomc.com/snapshots/")
    maven("https://repo.nexomc.com/releases/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.fancyinnovations.com/releases")
    mavenLocal()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":api-bukkit"))
    implementation(project(":paper"))
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("de.tr7zw:item-nbt-api:2.16.0")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    compileOnly("net.kyori:adventure-text-minimessage:5.2.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.4.1")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("io.papermc.paper:paper-api:26.2.build.112-stable")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.5") {
        exclude("org.spigotmc", "spigot-api")
    }
    compileOnly("me.filoghost.holographicdisplays:holographicdisplays-api:3.0.5")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.5.2")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude("org.bukkit", "bukkit")
    }
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.github.TownyAdvanced:Towny:0.98.3.6")
    compileOnly("com.github.Slimefun:Slimefun4:RC-37")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    compileOnly("com.nexomc:nexo:1.6.0")
    compileOnly("de.oliver:FancyHolograms:2.8.0")
    compileOnly("com.github.retrooper:packetevents-spigot:2.13.0")
    testImplementation(testFixtures(project(":common")))
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.2:4.116.1")
    testImplementation("io.papermc.paper:paper-api:26.2.build.112-stable")
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation("com.mysql:mysql-connector-j:9.3.0")
    testImplementation(platform("org.junit:junit-bom:5.13.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("net.kyori:adventure-text-minimessage:4.25.0")
    testImplementation("net.kyori:adventure-platform-bukkit:4.4.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val compiler = javaToolchains.compilerFor {
    languageVersion = JavaLanguageVersion.of(25)
}

val jetbrainsLauncher = javaToolchains.launcherFor {
    languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    withType<ShadowJar> {
        val projectVersion: String by project
        archiveFileName.set("Annaskills-${projectVersion}.jar")

        relocate("co.aikar.commands", "dev.aurelium.auraskills.acf")
        relocate("co.aikar.locales", "dev.aurelium.auraskills.locales")
        relocate("de.tr7zw.changeme.nbtapi", "dev.aurelium.auraskills.nbtapi")
        relocate("org.bstats", "dev.aurelium.auraskills.bstats")
        relocate("com.ezylang.evalex", "dev.aurelium.auraskills.evalex")
        relocate("net.kyori.option", "dev.aurelium.auraskills.kyori.option")
        relocate("com.zaxxer.hikari", "dev.aurelium.auraskills.hikari")
        relocate("dev.aurelium.slate", "dev.aurelium.auraskills.slate")
        relocate("org.spongepowered.configurate", "dev.aurelium.auraskills.configurate")
        relocate("io.leangen.geantyref", "dev.aurelium.auraskills.geantyref")
        relocate("net.querz", "dev.aurelium.auraskills.querz")
        relocate("com.archyx.polyglot", "dev.aurelium.auraskills.polyglot")
        relocate("org.atteo.evo.inflector", "dev.aurelium.auraskills.inflector")

        exclude("acf-*.properties")

        finalizedBy("copyJar")
    }

    register<Copy>("copyJar") {
        val projectVersion: String by project
        from("build/libs/Annaskills-${projectVersion}.jar")
        into("../build/libs")
    }

    build {
        dependsOn(shadowJar)
    }

    javadoc {
        options {
            (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
        options.compilerArgs.add("-Xlint:deprecation")
        options.isFork = true
        options.forkOptions.executable = compiler.map { it.executablePath }.get().toString()
    }

    val projectVersion = project.version.toString()

    processResources {
        filesMatching("plugin.yml") {
            expand("projectVersion" to projectVersion)
        }
    }

    withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
        javaLauncher = jetbrainsLauncher
        jvmArgs("-XX:+AllowEnhancedClassRedefinition")
    }

    runServer {
        minecraftVersion("26.2")
    }

    test {
        useJUnitPlatform()
    }
}
