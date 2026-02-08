# Redis refresh token support using spring-data-redis for security-jwt

This module adds support for storing refresh tokens at an external redis server.

Please note that the redis-plugin needs an external redis server.

The first version was primarily developed by [oiltea](https://github.com/oiltea), thank you!

# Dependencies
```xml
<dependency>
    <groupId>eu.fraho.spring</groupId>
    <artifactId>security-jwt-data-redis</artifactId>
    <version>5.3.0</version>
</dependency>
```

# Usage
* Add the dependency to your build script
* When not using the boot-starter: Use ```eu.fraho.spring.securityJwt.dataRedis.service.DataRedisTokenStore``` as ```fraho.jwt.refresh.cache-impl``` configuration value
* Basic configuration (e.g. host, port and authentication) is done using the spring-data-redis properties

This module also uses some additional application properties:

| Property                              | Default       | Description                                                                                                                                                                                                                                                                                                                                                    |
|---------------------------------------|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| fraho.jwt.refresh.redis.prefix        | fraho-refresh | Defines a common prefix for all saved refresh entries.                                                                                                                                                                                                                                                                                                         |
