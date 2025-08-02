import org.jreleaser.model.Active

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("org.jreleaser") version "1.18.0"
}

group = "io.github.ynverxe"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("org.spongepowered:configurate-core:4.0.0")

    testImplementation("org.spongepowered:configurate-yaml:4.0.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            repositories {
                maven {
                    url = uri(layout.buildDirectory.dir("staging"))
                    name = "Staging"
                }
            }

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom {
                name = "configurate-helper"
                description = "An utility library for https://github.com/SpongePowered/Configurate"
                url = "https://github.com/Ynverxe/configurate-helper"
                developers {
                    developer {
                        id = "Ynverxe"
                        email = "nahubar65@gmail.com"
                        url = "https://github.com/Ynverxe"
                        timezone = "GMT-3"
                    }
                }
                licenses {
                    license {
                        name = "MIT"
                        url = "https://opensource.org/license/mit"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/Ynverxe/configurate-helper.git"
                    developerConnection = "scm:git:ssh://github.com/Ynverxe/configurate-helper.git"
                    url = "https://github.com/Ynverxe/configurate-helper.git/tree/main"
                }
            }

            from(components["java"])
        }
    }
}

signing {
    if ("true" == System.getProperty("use-gpg-cmd", "false")) {
        useGpgCmd()
    } else {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
    sign(publishing.publications.getByName("maven"))
}

jreleaser {
    signing {
        active = Active.NEVER
    }

    deploy {
        maven {
            mavenCentral {
                create("central") {
                    applyMavenCentralRules = false
                    namespace = "io.github.ynverxe"
                    active = Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    username = System.getenv("CENTRAL_TOKEN_USERNAME")
                    password = System.getenv("CENTRAL_TOKEN_PASSWORD")
                    stagingRepository(layout.buildDirectory.dir("staging").get().asFile.absolutePath)
                }
            }
        }
    }
}
