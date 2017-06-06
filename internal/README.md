# In-Memory refresh token support for security-jwt

This module adds support for storing refresh tokens within an in-memory storage.

# Dependencies
```xml
<dependency>
    <groupId>eu.fraho.spring</groupId>
    <artifactId>security-jwt-internal</artifactId>
    <version>0.8.0</version>
</dependency>
```

# Usage
* Add the dependency to your build script
* Use ```eu.fraho.spring.securityJwt.service.InternalTokenStore``` as ```fraho.jwt.refresh.cache.impl``` configuration value

This module also uses some additional application properties:

| Property                                 | Default        | Description   |
|------------------------------------------|----------------|---------------|
| fraho.jwt.refresh.cache.delimiter        | :              | Use a custom delimiter for the refresh cache keys. The delimiter is used to separate the ```prefix```, ```username``` and ```deviceId```. These three values are taken for the primary key and forming an unique index. If no deviceId was provided by the client, "__default" will be used instead.|
