plugins {
	java
	id("io.freefair.lombok") version "8.6"
	id("org.springframework.boot") version "3.2.5"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.5"))

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")

	// Apache Kafka & Streams
	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.apache.kafka:kafka-streams")

	// testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")

	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.testcontainers:kafka")
}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("junit.jupiter.extensions.autodetection.enabled", true)
	systemProperty("junit.jupiter.execution.parallel.enabled", false)
}
