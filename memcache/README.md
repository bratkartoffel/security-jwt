# Memcache refresh token support for security-jwt

This module adds support for storing refresh tokens at an external memcache server.

Please note that the memcache-plugin needs an external memcached server.

# Dependencies
```xml
<dependency>
    <groupId>eu.fraho.spring</groupId>
    <artifactId>security-jwt-memcache</artifactId>
    <version>4.6.0</version>
</dependency>
```

# Usage
* Add the dependency to your build script
* When not using the boot-starter: Use ```eu.fraho.spring.securityJwt.memcache.service.MemcacheTokenStore``` as ```fraho.jwt.refresh.cache-impl``` configuration value

This module also uses some additional application properties:

| Property                                 | Default        | Description   |
|------------------------------------------|----------------|---------------|
| fraho.jwt.refresh.memcache.prefix        | fraho-refresh  | Defines a common prefix for all saved refresh entries. |
| fraho.jwt.refresh.memcache.host          | 127.0.0.1      | Hostname or IP Adress of memcache server|
| fraho.jwt.refresh.memcache.port          | 11211          | Port of memcache server|
| fraho.jwt.refresh.memcache.timeout       | 5              | Timeout (in seconds) when talking to memcache server|

This module also puts a contraint on the ```fraho.jwt.refresh.expiration``` property.
Due to protocol restrictions when communicating with the memcache server,
this field may not exceed 30 days.
