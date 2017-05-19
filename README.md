# Spring Security Addon for JWT
[![Build Status](https://travis-ci.org/bratkartoffel/security-jwt.svg?branch=develop)](https://travis-ci.org/bratkartoffel/security-jwt)
[![Code Coverage](https://img.shields.io/codecov/c/github/bratkartoffel/security-jwt/develop.svg)](https://codecov.io/github/bratkartoffel/security-jwt?branch=develop)
[![License](http://img.shields.io/:license-mit-blue.svg?style=flat)](http://doge.mit-license.org)

Simply integrate [JWT](https://jwt.io) into your spring boot application.

This project is split into 3 parts:

* base: Basic integration of JWT into spring security (without refresh tokens)
* internal: Support for an in-memory cache (ExpiringMap) for refresh tokens
* memcache: Support for memcache to store refresh tokens

Simply use the dependencies within your build script, spring boot takes care of the rest.
The default configuration should be sufficient for the most use cases.

# Contents
* base:
  * JWT Integration into Spring Security (including a [REST-Controller](base/src/main/java/eu/fraho/spring/securityJwt/controller/AuthenticationRestController.java) to authenticate against)
  * A [CryptPasswordEncoder](base/src/main/java/eu/fraho/spring/securityJwt/CryptPasswordEncoder.java) to generate / use linux system crypt(1)-hashes (supporting new $5$ and $6$ hashes and rounds)
  * Full support for [Swagger 2](https://github.com/springfox/springfox) documentation (REST Controller and DTO are annotated and described)
* internal:
  * Refresh token support through an internal, in-memory map
* memcache:
  * Refresh token support through an external memcache server

# Dependencies
```xml
<dependency>
    <groupId>eu.fraho.spring</groupId>
    <artifactId>security-jwt-base</artifactId>
    <version>0.6.0</version>
</dependency>
```

When you want to add refresh token support, then choose one of the following dependencies:
```xml
<dependency>
    <groupId>eu.fraho.spring</groupId>
    <artifactId>security-jwt-internal</artifactId>
    <version>0.6.0</version>
</dependency>
<dependency>
    <groupId>eu.fraho.spring</groupId>
    <artifactId>security-jwt-memcache</artifactId>
    <version>0.6.0</version>
</dependency>
```
Please note that the memcache-plugin needs an external memcached server.

# Usage
* Add the dependencies to your build script
* Configure your boot application to pick up our components (add "eu.fraho.spring.securityJwt" to the scanBasePackages field of your @SpringBootApplication)
* Create an implementation of UserDetailsService (you should have already done that) 
* Optionally use my enhanced PasswordEncoder as a @Bean
* Configure at least the JWT secrets (public + private keys or a hmac keyfile) in your application.yml

# Configuration
This library will not run out-of the box, it needs at least some information for the token security.
Either you create an ECDSA keypair for the tokens (you can use [this](base/src/test/java/eu/fraho/spring/securityJwt/util/CreateEcdsaJwtKeys.java) class for that)
or you change the used algorithm to HMAC and specify a secret keyfile.
This library is customizable by the following properties:

| Property                         | Default        | Description   |
|----------------------------------|----------------|---------------|
| fraho.jwt.token.algorithm        | ES256          | The signature algorithm used for the tokens. For a list of valid algorithms please see either the JWT spec or JWSAlgorithm.|
| fraho.jwt.token.issuer           | fraho-security | "Sets the issuer of the token. The issuer is used in the tokens "iss" field."|
| fraho.jwt.token.pub              | null           | Defines the public key file when using a public / private key signature method|
| fraho.jwt.token.priv             | null           | Defines the private key file when using a public / private key signature method. May be null if this service should only verify, but not issue tokens. In this case, calls to generateToken(JwtUser) will throw an IllegalArgumentException|
| fraho.jwt.token.hmac             | null           | Defines the key file when using a hmac signature method|
| fraho.jwt.token.expiration       | 1 hour         | The validity period of issued tokens. For details on how this field has to specified see TimeWithPeriod|
| fraho.jwt.refresh.expiration     | 1 day          | How long are refresh tokens valid? For details on how this field has to specified see TimeWithPeriod|
| fraho.jwt.refresh.length         | 24             | Defines the length of refresh tokens in bytes, without the base64 encoding|
| fraho.jwt.refresh.deviceIdLength | 32             | Maximum length of device ids for refresh tokens. Any longer strings will be truncated to this length.|
| fraho.jwt.refresh.cache.impl     | null           | Defines the implemenation for refresh token storage. The specified class has to implement the RefreshTokenStore Interface. To disable the refresh tokens at all use null as value.|
| fraho.jwt.refresh.cache.prefix   | fraho-refresh  | Defines a common prefix for all saved refresh entries. The map key is computed in the following way: <prefix>:<username>:<deviceId>. If no deviceId was provided by the client, "__default" will be used instead.).|
| fraho.totp.variance              | 3              | "Defines the allowed variance / validity of totp pins. The number defines how many "old / expired" pins will be considered valid. A value of "3" is the official suggestion for TOPT. This value is used to consider small clock-differences between client and server."|
| fraho.totp.length                | 16             | Defines the length of the generated totp secrets.|
| fraho.crypt.rounds               | 10000          | Defines the "strength" of the hashing function. The more rounds used, the more secure the generated hash. But beware that more rounds mean more cpu-load and longer computation times!|
| fraho.crypt.algorithm            | SHA512         | Configured the used crypt algorithm. For a list of possible values see CryptAlgorithm|

Additional properties when using the memcache module:

| Property                                 | Default   | Description   |
|------------------------------------------|-----------|---------------|
| fraho.jwt.refresh.cache.memcache.host    | 127.0.0.1 | Hostname or IP Adress of memcache server|
| fraho.jwt.refresh.cache.memcache.port    | 11211     | Port of memcache server|
| fraho.jwt.refresh.cache.memcache.timeout | 5         | Timeout (in seconds) when talking to memcache server|

# Building
```bash
# on linux:
./gradlew assemble
# on windows:
gradlew.bat assemble
```

# Hacking
* This repository uses the git flow layout
* Changes are welcome, but please use pull requests with separate branches
* TravisCI has to pass before merging
* Code coverage should stay about the same level (please write tests for new features)

# Releasing
```bash
# to local repository:
./gradlew install
# to central:
./gradlew -Prelease check uploadArchives
```
