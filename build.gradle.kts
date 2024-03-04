

import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.Property
import java.util.*

val props = Properties()
File("src/main/resources/application-db.properties").inputStream().use {
	props.load(it)
}



val dbUrl: String = props.getProperty("spring.datasource.url")
val dbSchema: String = props.getProperty("spring.datasource.schema")
val dbUsername: String = props.getProperty("spring.datasource.username")
val dbPassword: String = props.getProperty("spring.datasource.password")
val dbDriverClassName: String = props.getProperty("spring.datasource.driver_class_name")


plugins {
	java
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"
	id("nu.studer.jooq") version "9.0"
}

// Set the jOOQ version for Spring Boot plugin
ext["jooq.version"] = "3.19.1"

group = "de.batschko"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("org.mariadb.jdbc:mariadb-java-client:2.7.4")
	implementation("org.springframework.boot:spring-boot-starter-web")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	// jsoup HTML parser library @ https://jsoup.org/
	implementation("org.jsoup:jsoup:1.17.2")
	compileOnly("org.projectlombok:lombok:1.18.30")
	annotationProcessor("org.projectlombok:lombok:1.18.30")
	implementation("com.google.guava:guava:33.0.0-jre")
	jooqGenerator("org.mariadb.jdbc:mariadb-java-client:2.7.4")
	implementation("com.googlecode.json-simple:json-simple:1.1.1")
	implementation("org.json:json:20171018")


	implementation ("org.seleniumhq.selenium:selenium-java:4.18.1")

}

tasks.withType<Test> {
	useJUnitPlatform()
}

jooq {

	version.set("3.19.1")  // default (can be omitted)
	edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)  // default (can be omitted)

	configurations {
		create("main") {  // name of the jOOQ configuration
			generateSchemaSourceOnCompilation.set(true)  // default (can be omitted)

			jooqConfiguration.apply {
				logging = Logging.WARN
				jdbc.apply {
					driver = dbDriverClassName
					url = dbUrl
					user = dbUsername
					password = dbPassword
					properties.add(Property().apply {
						key = "ssl"
						value = "true"
					})
				}
				generator.apply {
					name = "org.jooq.codegen.DefaultGenerator"
					database.apply {
						name = "org.jooq.meta.mariadb.MariaDBDatabase"
						inputSchema = dbSchema
						forcedTypes.addAll(listOf(
								ForcedType().apply {
									userType = "de.batschko.tradeupproject.enums.Rarity"
									isEnumConverter = true
									includeExpression = "Rarity"
								},
								ForcedType().apply {
									userType = "de.batschko.tradeupproject.enums.Condition"
									isEnumConverter = true
									includeExpression = "Condition|condition_target"
								},
								ForcedType().apply {
									userType = "de.batschko.tradeupproject.enums.TradeUpStatus"
									isEnumConverter = true
									includeExpression = "status"
								},
								ForcedType().apply {
									userType = "de.batschko.tradeupproject.enums.PriceType"
									isEnumConverter = true
									includeExpression = "price_type"
								},
						))
					}
					generate.apply {
						isDeprecated = false
						isRecords = true
						isImmutablePojos = false
						isFluentSetters = true
					}
					target.apply {
						packageName = "de.batschko.tradeupproject"
					//	directory = "build/generated-src/jooq/main"  // default (can be omitted)
						directory = "src/main/java/db"
					}
					strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
				}
			}
		}
	}
}