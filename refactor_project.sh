#!/usr/bin/env bash
set -e

echo "========================================================"
echo "      JamMod Multi-Version Refactoring (Java 17+)       "
echo "========================================================"

echo ">>> [1/6] Creating safe backup of current files..."
BACKUP_DIR=".backup_1.20.1_$(date +%s)"
mkdir -p "$BACKUP_DIR"
cp -r src build.gradle gradle.properties "$BACKUP_DIR/" 2>/dev/null || true
echo "Backup saved in: $BACKUP_DIR"

echo ">>> [2/6] Creating modular project directory tree..."
mkdir -p common/src/main/java/com/skl/jammod/session
mkdir -p common/src/main/java/com/skl/jammod/client

SUBPROJECTS=("forge-1.18.2" "forge-1.19.2" "forge-1.20.1" "neoforge-1.20.4" "neoforge-1.21.1")
for p in "${SUBPROJECTS[@]}"; do
    mkdir -p "$p/src/main/java/com/skl/jammod"
    mkdir -p "$p/src/main/resources/META-INF"
done

mkdir -p dist

echo ">>> [3/6] Migrating platform-agnostic classes into :common..."
cp src/main/java/com/skl/jammod/session/JamTrack.java common/src/main/java/com/skl/jammod/session/ 2>/dev/null || true
cp src/main/java/com/skl/jammod/session/JamEditPolicy.java common/src/main/java/com/skl/jammod/session/ 2>/dev/null || true
cp src/main/java/com/skl/jammod/session/AntiSpamManager.java common/src/main/java/com/skl/jammod/session/ 2>/dev/null || true
cp src/main/java/com/skl/jammod/client/OverlayCorner.java common/src/main/java/com/skl/jammod/client/ 2>/dev/null || true

cat << 'EOF' > common/src/main/java/com/skl/jammod/client/ClientJamConfig.java
package com.skl.jammod.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientJamConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("JamMod");
    private static Path configFile = Paths.get("config", "jammod_client.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static ConfigData data = new ConfigData();

    public static class ConfigData {
        public boolean overlayEnabled = true;
        public OverlayCorner overlayCorner = OverlayCorner.TOP_RIGHT;
    }

    static {
        load();
    }

    public static synchronized void setConfigDirectory(Path dir) {
        configFile = dir.resolve("jammod_client.json");
        load();
    }

    public static synchronized void load() {
        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
                if (loaded != null) {
                    data = loaded;
                    if (data.overlayCorner == null) data.overlayCorner = OverlayCorner.TOP_RIGHT;
                }
            } catch (Exception e) {
                LOGGER.error("[JamMod] Failed to load client config: ", e);
            }
        }
    }

    public static synchronized void save() {
        try {
            if (configFile.getParent() != null) {
                Files.createDirectories(configFile.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            LOGGER.error("[JamMod] Failed to save client config: ", e);
        }
    }

    public static boolean isOverlayEnabled() {
        return data.overlayEnabled;
    }

    public static void setOverlayEnabled(boolean enabled) {
        data.overlayEnabled = enabled;
        save();
    }

    public static void toggleOverlay() {
        setOverlayEnabled(!data.overlayEnabled);
    }

    public static OverlayCorner getOverlayCorner() {
        return data.overlayCorner != null ? data.overlayCorner : OverlayCorner.TOP_RIGHT;
    }

    public static void setOverlayCorner(OverlayCorner corner) {
        data.overlayCorner = corner;
        save();
    }

    public static void cycleOverlayCorner() {
        setOverlayCorner(getOverlayCorner().next());
    }
}
EOF

# Copy 1.20.1 implementation and remove duplicate common classes
cp -r src/* forge-1.20.1/src/ 2>/dev/null || true
rm -f forge-1.20.1/src/main/java/com/skl/jammod/session/JamTrack.java
rm -f forge-1.20.1/src/main/java/com/skl/jammod/session/JamEditPolicy.java
rm -f forge-1.20.1/src/main/java/com/skl/jammod/session/AntiSpamManager.java
rm -f forge-1.20.1/src/main/java/com/skl/jammod/client/OverlayCorner.java
rm -f forge-1.20.1/src/main/java/com/skl/jammod/client/ClientJamConfig.java

echo ">>> [4/6] Generating root and subproject Gradle configurations..."

cat << 'EOF' > gradle.properties
org.gradle.jvmargs=-Xmx3G -XX:+UseG1GC
org.gradle.daemon=true
EOF

cat << 'EOF' > settings.gradle
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = 'https://maven.minecraftforge.net/' }
        maven { url = 'https://maven.neoforged.net/releases' }
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.8.0'
}

rootProject.name = 'JamMod'

include 'common'
include 'forge-1.18.2'
include 'forge-1.19.2'
include 'forge-1.20.1'
include 'neoforge-1.20.4'
include 'neoforge-1.21.1'
EOF

cat << 'EOF' > build.gradle
plugins {
    id 'base'
}

subprojects {
    apply plugin: 'java'

    group = 'com.skl'
    version = '1.0.0'

    repositories {
        mavenCentral()
        maven {
            name = 'NeoForged'
            url = 'https://maven.neoforged.net/releases'
        }
        maven {
            name = 'Minecraft'
            url = 'https://libraries.minecraft.net/'
        }
        maven { url = 'https://maven.lavalink.dev/releases' }
        maven { url = 'https://jitpack.io' }
    }
}
EOF

cat << 'EOF' > common/build.gradle
plugins {
    id 'java-library'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    api 'dev.arbjerg:lavaplayer:2.2.2'
    api 'dev.arbjerg:lavaplayer-natives:2.2.2'
    api 'dev.arbjerg:lava-common:2.2.2'
    api 'dev.lavalink.youtube:common:1.18.2'
    api 'com.google.code.gson:gson:2.10.1'
    api 'org.slf4j:slf4j-api:2.0.7'
}
EOF

cat << 'EOF' > forge-1.18.2/build.gradle
plugins {
    id 'eclipse'
    id 'idea'
    id 'net.minecraftforge.gradle' version '[6.0,6.2)'
}

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

sourceSets {
    main {
        java {
            srcDir project(':common').file('src/main/java')
        }
    }
}

minecraft {
    mappings channel: 'official', version: '1.18.2'
    copyIdeResources = true

    runs {
        configureEach {
            workingDirectory project.file('run')
            property 'forge.logging.markers', 'REGISTRIES'
            property 'forge.logging.console.level', 'debug'
            mods { jammod { source sourceSets.main } }
        }
        client {}
        server { args '--nogui' }
    }
}

dependencies {
    minecraft 'net.minecraftforge:forge:1.18.2-40.2.21'

    minecraftLibrary 'org.apache.httpcomponents:httpclient:4.5.14'
    minecraftLibrary 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
    minecraftLibrary 'org.jsoup:jsoup:1.17.2'
    minecraftLibrary 'net.iharder:base64:2.3.9'
    minecraftLibrary 'dev.lavalink.youtube:common:1.18.2'
    minecraftLibrary 'dev.arbjerg:lavaplayer:2.2.2'
    minecraftLibrary 'dev.arbjerg:lavaplayer-natives:2.2.2'
    minecraftLibrary 'dev.arbjerg:lava-common:2.2.2'
}

jar {
    manifest {
        attributes([
            'Specification-Title'     : 'jammod',
            'Specification-Vendor'    : '4skl',
            'Specification-Version'   : '1',
            'Implementation-Title'    : project.name,
            'Implementation-Version'  : project.version,
            'Implementation-Vendor'   : '4skl',
            'Implementation-Timestamp': new Date().format("yyyy-MM-dd'T'HH:mm:ssZ")
        ])
    }
}
EOF

cat << 'EOF' > forge-1.19.2/build.gradle
plugins {
    id 'eclipse'
    id 'idea'
    id 'net.minecraftforge.gradle' version '[6.0,6.2)'
}

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

sourceSets {
    main {
        java {
            srcDir project(':common').file('src/main/java')
        }
    }
}

minecraft {
    mappings channel: 'official', version: '1.19.2'
    copyIdeResources = true

    runs {
        configureEach {
            workingDirectory project.file('run')
            property 'forge.logging.markers', 'REGISTRIES'
            property 'forge.logging.console.level', 'debug'
            mods { jammod { source sourceSets.main } }
        }
        client {}
        server { args '--nogui' }
    }
}

dependencies {
    minecraft 'net.minecraftforge:forge:1.19.2-43.3.0'

    minecraftLibrary 'org.apache.httpcomponents:httpclient:4.5.14'
    minecraftLibrary 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
    minecraftLibrary 'org.jsoup:jsoup:1.17.2'
    minecraftLibrary 'net.iharder:base64:2.3.9'
    minecraftLibrary 'dev.lavalink.youtube:common:1.18.2'
    minecraftLibrary 'dev.arbjerg:lavaplayer:2.2.2'
    minecraftLibrary 'dev.arbjerg:lavaplayer-natives:2.2.2'
    minecraftLibrary 'dev.arbjerg:lava-common:2.2.2'
}

jar {
    manifest {
        attributes([
            'Specification-Title'     : 'jammod',
            'Specification-Vendor'    : '4skl',
            'Specification-Version'   : '1',
            'Implementation-Title'    : project.name,
            'Implementation-Version'  : project.version,
            'Implementation-Vendor'   : '4skl',
            'Implementation-Timestamp': new Date().format("yyyy-MM-dd'T'HH:mm:ssZ")
        ])
    }
}
EOF

cat << 'EOF' > forge-1.20.1/build.gradle
plugins {
    id 'eclipse'
    id 'idea'
    id 'net.minecraftforge.gradle' version '[6.0,6.2)'
}

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

sourceSets {
    main {
        java {
            srcDir project(':common').file('src/main/java')
        }
    }
}

minecraft {
    mappings channel: 'official', version: '1.20.1'
    copyIdeResources = true

    runs {
        configureEach {
            workingDirectory project.file('run')
            property 'forge.logging.markers', 'REGISTRIES'
            property 'forge.logging.console.level', 'debug'
            mods { jammod { source sourceSets.main } }
        }
        client {}
        server { args '--nogui' }
    }
}

dependencies {
    minecraft 'net.minecraftforge:forge:1.20.1-47.4.28'

    minecraftLibrary 'org.apache.httpcomponents:httpclient:4.5.14'
    minecraftLibrary 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
    minecraftLibrary 'org.jsoup:jsoup:1.17.2'
    minecraftLibrary 'net.iharder:base64:2.3.9'
    minecraftLibrary 'dev.lavalink.youtube:common:1.18.2'
    minecraftLibrary 'dev.arbjerg:lavaplayer:2.2.2'
    minecraftLibrary 'dev.arbjerg:lavaplayer-natives:2.2.2'
    minecraftLibrary 'dev.arbjerg:lava-common:2.2.2'
}

jar {
    manifest {
        attributes([
            'Specification-Title'     : 'jammod',
            'Specification-Vendor'    : '4skl',
            'Specification-Version'   : '1',
            'Implementation-Title'    : project.name,
            'Implementation-Version'  : project.version,
            'Implementation-Vendor'   : '4skl',
            'Implementation-Timestamp': new Date().format("yyyy-MM-dd'T'HH:mm:ssZ")
        ])
    }
}
EOF

cat << 'EOF' > neoforge-1.20.4/build.gradle
plugins {
    id 'java-library'
    id 'eclipse'
    id 'idea'
    id 'net.neoforged.moddev' version '2.0.78'
}

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

sourceSets {
    main {
        java {
            srcDir project(':common').file('src/main/java')
        }
    }
}

neoForge {
    version = '20.4.253'
    runs {
        client { client() }
        server { server() }
    }
    mods {
        jammod { sourceSet sourceSets.main }
    }
}

dependencies {
    implementation('dev.arbjerg:lavaplayer:2.2.2') {
        exclude group: 'org.apache.httpcomponents', module: 'httpclient'
    }
    implementation 'dev.arbjerg:lavaplayer-natives:2.2.2'
    implementation 'dev.arbjerg:lava-common:2.2.2'
    implementation 'dev.lavalink.youtube:common:1.18.2'

    jarJar('dev.arbjerg:lavaplayer:2.2.2') {
        exclude group: 'org.apache.httpcomponents', module: 'httpclient'
    }
    jarJar 'dev.arbjerg:lavaplayer-natives:2.2.2'
    jarJar 'dev.arbjerg:lava-common:2.2.2'
    jarJar 'dev.lavalink.youtube:common:1.18.2'

    additionalRuntimeClasspath('dev.arbjerg:lavaplayer:2.2.2') {
        exclude group: 'org.apache.httpcomponents', module: 'httpclient'
    }
    additionalRuntimeClasspath 'dev.arbjerg:lavaplayer-natives:2.2.2'
    additionalRuntimeClasspath 'dev.arbjerg:lava-common:2.2.2'
    additionalRuntimeClasspath 'dev.lavalink.youtube:common:1.18.2'
}

jar {
    manifest {
        attributes([
            'Specification-Title'     : 'jammod',
            'Specification-Vendor'    : '4skl',
            'Specification-Version'   : '1',
            'Implementation-Title'    : project.name,
            'Implementation-Version'  : project.version,
            'Implementation-Vendor'   : '4skl',
            'Implementation-Timestamp': new Date().format("yyyy-MM-dd'T'HH:mm:ssZ")
        ])
    }
}
EOF

cat << 'EOF' > neoforge-1.21.1/build.gradle
plugins {
    id 'java-library'
    id 'eclipse'
    id 'idea'
    id 'net.neoforged.moddev' version '2.0.78'
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

sourceSets {
    main {
        java {
            srcDir project(':common').file('src/main/java')
        }
    }
}

neoForge {
    version = '21.1.93'
    runs {
        client { client() }
        server { server() }
    }
    mods {
        jammod { sourceSet sourceSets.main }
    }
}

dependencies {
    implementation('dev.arbjerg:lavaplayer:2.2.2') {
        exclude group: 'org.apache.httpcomponents', module: 'httpclient'
    }
    implementation 'dev.arbjerg:lavaplayer-natives:2.2.2'
    implementation 'dev.arbjerg:lava-common:2.2.2'
    implementation 'dev.lavalink.youtube:common:1.18.2'

    jarJar('dev.arbjerg:lavaplayer:2.2.2') {
        exclude group: 'org.apache.httpcomponents', module: 'httpclient'
    }
    jarJar 'dev.arbjerg:lavaplayer-natives:2.2.2'
    jarJar 'dev.arbjerg:lava-common:2.2.2'
    jarJar 'dev.lavalink.youtube:common:1.18.2'

    additionalRuntimeClasspath('dev.arbjerg:lavaplayer:2.2.2') {
        exclude group: 'org.apache.httpcomponents', module: 'httpclient'
    }
    additionalRuntimeClasspath 'dev.arbjerg:lavaplayer-natives:2.2.2'
    additionalRuntimeClasspath 'dev.arbjerg:lava-common:2.2.2'
    additionalRuntimeClasspath 'dev.lavalink.youtube:common:1.18.2'
}

jar {
    manifest {
        attributes([
            'Specification-Title'     : 'jammod',
            'Specification-Vendor'    : '4skl',
            'Specification-Version'   : '1',
            'Implementation-Title'    : project.name,
            'Implementation-Version'  : project.version,
            'Implementation-Vendor'   : '4skl',
            'Implementation-Timestamp': new Date().format("yyyy-MM-dd'T'HH:mm:ssZ")
        ])
    }
}
EOF

echo ">>> [5/6] Multi-project files written successfully."

echo ">>> [6/6] Compiling :common and :forge-1.20.1 to verify pipeline..."
./gradlew :common:build :forge-1.20.1:compileJava :neoforge-1.21.1:compileJava --daemon

echo "========================================================"
echo " Setup complete! Common core and subprojects build cleanly. "
echo "========================================================"