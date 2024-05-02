plugins {
    id("java")
    id("groovy")
}

group = "io.github.jpautils"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.slf4j:slf4j-api:2.0.13")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation(platform("org.apache.groovy:groovy-bom:4.0.20"))
    testImplementation("org.apache.groovy:groovy")
    testImplementation(platform("org.spockframework:spock-bom:2.3-groovy-4.0"))
    testImplementation("org.spockframework:spock-core")
    //byte-buddy and objenesis are used by Spock to create mocks
    testImplementation("net.bytebuddy:byte-buddy:1.14.13")
    testImplementation("org.objenesis:objenesis:3.3")
}

tasks.test {
    useJUnitPlatform()
}