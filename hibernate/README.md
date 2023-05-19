# Hibernate refresh token support for security-jwt

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
    <version>5.0.5</version>
</dependency>
```

# Usage
* Add the dependency to your build script
* When not using the boot-starter:
  * Use ```eu.fraho.spring.securityJwt.hibernate.service.HibernateTokenStore``` as ```fraho.jwt.refresh.cache-impl``` configuration value
  * Configure your boot application to pick up our entities (add ```@EntityScan(basePackages = {"eu.fraho.spring.securityJwt"})``` annotation asside your ```@SpringBootApplication```)

This module doesn't use any additional application properties.

# Database schema
Please use your hibernate ```ddl-auto```-Property to generate the needed refresh token table.

If you need or want to create the table manually, then please take this SQL and change it to accodingly to your database system:
```mysql
-- Create the table
CREATE TABLE jwt_refresh (
    id         bigint       AUTO_INCREMENT PRIMARY KEY NOT NULL,
    created    timestamp    NOT NULL,
    user_id    bigint       NOT NULL,
    username   varchar(255) NOT NULL,
    token      varchar(255) NOT NULL
);
```
