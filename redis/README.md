# Redis refresh token support for security-jwt

This module adds support for storing refresh tokens at an external redis server.

Please note that the redis-plugin needs an external redisd server.

# Dependencies
```xml
<dependency>
    <groupId>eu.fraho.spring</groupId>
    <artifactId>security-jwt-redis</artifactId>
    <version>5.0.1</version>
</dependency>
```

# Usage
* Add the dependency to your build script
* When not using the boot-starter: Use ```eu.fraho.spring.securityJwt.redis.service.RedisTokenStore``` as ```fraho.jwt.refresh.cache-impl``` configuration value

This module also uses some additional application properties:

| Property                              | Default       | Description                                                                                                                                                                                                                                                                                                                                                    |
|---------------------------------------|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| fraho.jwt.refresh.redis.prefix        | fraho-refresh | Defines a common prefix for all saved refresh entries.                                                                                                                                                                                                                                                                                                         |
| fraho.jwt.refresh.redis.host          | 127.0.0.1     | Hostname or IP Adress of redis server                                                                                                                                                                                                                                                                                                                          |
| fraho.jwt.refresh.redis.port          | 6379          | Port of redis server                                                                                                                                                                                                                                                                                                                                           |
| fraho.jwt.refresh.redis.pool-config.* | various       | This field can be used to tune the connection pool to redis. This class is directly taken the jedis library. Please see [the fields here](https://static.javadoc.io/redis.clients/jedis/2.9.0/redis/clients/jedis/JedisPoolConfig.html#methods.inherited.from.class.org.apache.commons.pool2.impl.GenericObjectPoolConfig) for possible configuration options. |
