# Hibernate refresh token support for [security-jwt](../)

This module adds support for storing refresh tokens within
your applications database.

The table used to store the tokens is hardcoded as "jwt_refresh".

As a normal database don't support expiration and automatic deletion
for rows you have to regularly cleanup the token table, e.g. by using a cronjob.

# Dependencies
```xml
<dependency>
    <groupId>eu.fraho.spring</groupId>
    <artifactId>security-jwt-hibernate</artifactId>
    <version>0.6.0</version>
</dependency>
```

# Usage
* Add the dependency to your build script
* Use ```eu.fraho.spring.securityJwt.service.HibernateTokenStore``` as ```fraho.jwt.refresh.cache.impl``` configuration value
* Configure your boot application to pick up our entities (add ```@EntityScan(basePackages = {"eu.fraho.spring.securityJwt"})``` annotation asside your ```@SpringBootApplication```)

This module doesn't use any additional application properties.
