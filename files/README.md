# Filesystem refresh token support for security-jwt

This module adds support for storing refresh tokens within an simple json document on the filesystem.

# Dependencies
```xml
<dependency>
    <groupId>eu.fraho.spring</groupId>
    <artifactId>security-jwt-files</artifactId>
    <version>4.5.0</version>
</dependency>
```

# Usage
* Add the dependency to your build script
* When not using the boot-starter: Use ```eu.fraho.spring.securityJwt.files.service.FilesTokenStore``` as ```fraho.jwt.refresh.cache-impl``` configuration value

This module also uses some additional application properties:

| Property                              | Default        | Description   |
|---------------------------------------|----------------|---------------|
| fraho.jwt.refresh.files.dataDir       | data/          | The directory where the database and lockfile (if used) are stored. |
| fraho.jwt.refresh.files.externalLocks | false          | Use a filesystem-level lockfile? Due to performance reasons this should only be used when other applications access the database. |
| fraho.jwt.refresh.files.databaseFile  | db.json        | Filename of the database file, relative to dataDir. |
