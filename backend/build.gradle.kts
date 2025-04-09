plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.0'
    id 'java'
}

group = 'com.eliza'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    
    // WebSocket
    implementation 'org.webjars:webjars-locator-core'
    implementation 'org.webjars:sockjs-client:1.5.1'
    implementation 'org.webjars:stomp-websocket:2.3.3'
    implementation 'org.webjars:bootstrap:5.1.3'
    implementation 'org.webjars:jquery:3.6.0'
    
    // PostgreSQL
    implementation 'org.postgresql:postgresql'
    
    // Firebase Admin
    implementation 'com.google.firebase:firebase-admin:9.1.1'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt:0.9.1'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
