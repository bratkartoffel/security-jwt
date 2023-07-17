# Spring Security Addon for JWT

[![Java CI](https://github.com/bratkartoffel/security-jwt/actions/workflows/build.yaml/badge.svg)](https://github.com/bratkartoffel/security-jwt/actions/workflows/build.yaml)
[![Code Coverage](https://img.shields.io/codecov/c/github/bratkartoffel/security-jwt/develop.svg)](https://codecov.io/github/bratkartoffel/security-jwt?branch=develop)
[![License](http://img.shields.io/:license-mit-blue.svg?style=flat)](http://doge.mit-license.org)
[![Central Version](https://img.shields.io/maven-central/v/eu.fraho.spring/security-jwt-base)](https://mvnrepository.com/artifact/eu.fraho.spring)

Providing a simple way to integrate [JWT](https://jwt.io/introduction/) into your spring boot application.

This project is split into multiple parts:

* base: Basic integration of JWT into spring security (without refresh tokens)
* internal: Support for an in-memory cache (ExpiringMap) for refresh tokens
* memcache: Support for memcache to store refresh tokens
* hibernate: Support for hibernate to store refresh tokens
* redis: Support for redis to store refresh tokens
* files: Support for filesystem to store refresh tokens

Simply use the dependencies within your build script, spring boot takes care of the rest. The default configuration
should be sufficient for the most use cases.

# Contents

* base:
  * JWT Integration into Spring Security (including
    some [REST-Controllers](base/src/main/java/eu/fraho/spring/securityJwt/base/controller) to authenticate against)
  * A [CryptPasswordEncoder](base/src/main/java/eu/fraho/spring/securityJwt/base/password/CryptPasswordEncoder.java)
    to generate / use linux system crypt(1)-hashes (supporting the newer $5$ and $6$ hashes and rounds)
  * Full support for [Swagger 2](https://github.com/springfox/springfox) documentation (REST Controller and DTO are
    annotated and described)
* module [internal](internal):
  * Refresh token support through an internal, in-memory map
* module [memcache](memcache):
  * Refresh token support through an external memcache server
* module [hibernate](hibernate):
  * Refresh token support using hibernate and a database table
* module [redis](redis):
  * Refresh token support using a redis server
* module [files](files):
  * Refresh token support using a json file
* various *-spring-boot-starter:
    * Spring boot starter modules to integrate into the autoconfiguration ecosystem

# Dependencies

```xml

<dependencies>
    <dependency>
        <groupId>eu.fraho.spring</groupId>
        <artifactId>security-jwt-base</artifactId>
        <version>4.6.8</version>
    </dependency>
    <!-- or -->
    <dependency>
        <groupId>eu.fraho.spring</groupId>
        <artifactId>security-jwt-base-spring-boot-starter</artifactId>
        <version>4.6.8</version>
    </dependency>
</dependencies>
```

When you want to add refresh token support, then choose one of the following dependencies:

```xml

<dependencies>
    <dependency>
        <groupId>eu.fraho.spring</groupId>
        <artifactId>security-jwt-internal</artifactId>
        <version>4.6.8</version>
    </dependency>
    <dependency>
        <groupId>eu.fraho.spring</groupId>
        <artifactId>security-jwt-memcache</artifactId>
        <version>4.6.8</version>
    </dependency>
    <dependency>
        <groupId>eu.fraho.spring</groupId>
        <artifactId>security-jwt-hibernate</artifactId>
        <version>4.6.8</version>
    </dependency>
    <dependency>
        <groupId>eu.fraho.spring</groupId>
        <artifactId>security-jwt-redis</artifactId>
        <version>4.6.8</version>
    </dependency>
    <dependency>
        <groupId>eu.fraho.spring</groupId>
        <artifactId>security-jwt-files</artifactId>
        <version>4.6.8</version>
    </dependency>
</dependencies>
```

For details on the usage of the plugins please see the README within the relevant module directories.

# Usage

Starting with version 1.0.0 there are two ways on how to use these libraries.

The old way is by directly using the libraries as dependencies and doing some manual configuration. The newer way used
spring boot autoconfiguration and reduced the needed configuration a lot.

To see this library "in action", please take a look
at [the examples](https://github.com/bratkartoffel/security-jwt-examples).

# Spring boot and library versions

The mimum supported spring boot version changed during the lifetime of this library and sadly wasn't always aligned to
the semantic versioning scheme. Take the following table of supported versions as reference prior updating this library
in your project.

| Library Version | Spring boot versions |
|-----------------|----------------------|
| `< 3.0.0`       | [1.5.0, 2.0.0[       |
| `3.0.0 - 4.4.0` | [2.0.0, 2.2.0[       |
| `4.4.1 - 4.4.2` | [2.2.0, 2.6.0[       |
| `4.5.x - 4.6.x` | [2.0.0, 3.0.0[       |

## Spring Boot Autoconfig (recommended):

* Use any *-spring-boot-starter dependency you like
* Bouncycastle will be automagically loaded and installed if on classpath
* My enhanced PasswordEncoder (using Unix-Crypt-Style hashes) will be used as default

## Manual configuration (legacy):

* Add the dependencies to your build script
* Configure your boot application to pick up our components (add "eu.fraho.spring.securityJwt" to the scanBasePackages
  field of your ```@SpringBootApplication```)
* Optionally add the BouncyCastle Provider (e.g. within
  the [main-Method](base/src/testFixtures/java/eu/fraho/spring/securityJwt/base/util/CreateEcdsaJwtKeys.java))
  * **Hint:** This is required if you would like to use the ECDSA signature algorithm!
* Optionally use my enhanced PasswordEncoder as a ```@Bean```
* Optionally choose a refresh token store implementation and set it as ```fraho.jwt.refresh.cache-impl```

## General steps for both methods:

* Create an implementation of UserDetailsService that returns an instance
  of [JwtUser](base/src/main/java/eu/fraho/spring/securityJwt/base/dto/JwtUser.java)
* By default, this service creates a random hmac secret for signatures on each startup. To stay consistent accross
  service restarts and not kicking clients out please either change the algorithm (recommended) or specifiy at least an
  hmac keyfile.

# Usage for clients

* Request new tokens by sending an authentication request to ```/auth/login```
* Somehow store the received token(s)
  * Or use the cookie flow so the browser can store the tokens safely
* To access a secured controller set the retrieved token as ```Authorization: <TokenType> <Token>```
  * When using cookies this is done automatically by your browser

# Configuration

This library will run out-of the box, but you should at least take a look at the different configuration properties. I
tried to make them reasonable secure, but if you're really paranoid you can change them as you like.

By default, this library creates an hmac secret on startup. As this is no problem for testing and running your
application you are strongly adviced to define a static key. Otherwise you will render all access tokens invalid upon
service restart, thus requiring your clients to login again.

I recommend using ECDSA for tokens (you can
use [this](base/src/testFixtures/java/eu/fraho/spring/securityJwt/base/util/CreateEcdsaJwtKeys.java) class for that) and
setting the ```algorithm``` field to something like ES256.

## Token configuration (Prefix fraho.jwt.token)

| Property        | Default                     | Description   |
|-----------------|-----------------------------|---------------|
| algorithm       | HS256                       | The signature algorithm used for the tokens. For a list of valid algorithms please see either the [JWT spec](https://tools.ietf.org/html/rfc7518#section-3) or [JWSAlgorithm](https://bitbucket.org/connect2id/nimbus-jose-jwt/src/master/src/main/java/com/nimbusds/jose/JWSAlgorithm.java)|
| cookie.enabled  | false                       | Enables support for tokens sent as a cookie|
| cookie.names    | JWT-ACCESSTOKEN, XSRF-TOKEN | Sets the name of the cookie with the token. The first entry in this list is used when sending out the cookie, any other names are optionally taken when validating incoming requests.|
| cookie.domain   | null                        | The issued tokens will only be valid for the specified domain. Defaults to the issuing server domain.|
| cookie.httpOnly | true                        | The cookie will not be accessible by client JavaScript if enabled (highly recommend)|
| cookie.path     | /                           | The issued access token cookie will only be sent by the client to URIs matching this pattern|
| cookie.secure   | true                        | The cookie will only be sent over an encrypted (https) connection (highly recommend)|
| expiration      | 1 hour                      | The validity period of issued tokens. For details on how this field has to specified see [TimeWithPeriod](base/src/main/java/eu/fraho/spring/securityJwt/base/dto/TimeWithPeriod.java)|
| header.enabled  | true                        | Enables support for tokens sent as a header. (highly recommended)|
| header.names    | Authorization               | Sets the name of the headers which may contain the token.|
| hmac            | null                        | Defines the key file when using a hmac signature method|
| issuer          | fraho-security              | Sets the issuer of the token. The issuer is used in the tokens ```iss``` field|
| path            | /auth/login                 | Sets the path for the RestController, defining the endpoint for login requests.|
| priv            | null                        | Defines the private key file when using a public / private key signature method. May be null if this service should only verify, but not issue tokens. In this case, any calls to ```generateToken``` or ```generateRefreshToken``` will throw an FeatureNotConfiguredException. To the caller, it will be shown as a UNAUTHORIZED Http StatusCode.|
| pub             | null                        | Defines the public key file when using a public / private key signature method|

## Refresh configuration (Prefix fraho.jwt.refresh)

| Property                   | Default             | Description   |
|----------------------------|---------------------|---------------|
| cache-impl                 | null                | Defines the implemenation for refresh token storage. The specified class has to implement the [RefreshTokenStore](base/src/main/java/eu/fraho/spring/securityJwt/base/service/RefreshTokenStore.java) Interface. To disable the refresh tokens at all use null as value.<br>You have to add at least one of the optional dependencies below to add refresh token support.<br>Please see module READMEs for valid values.|
| cookie.enabled             | false               | Enables support for tokens sent as a cookie|
| cookie.names               | JWT-REFRESHTOKEN    | Sets the name of the cookie with the token. The first entry in this list is used when sending out the cookie, any other names are optionally taken when validating incoming requests.|
| cookie.domain              | null                | If this value is not specified, then the redirect for refreshing the cookies will be sent only with the path specified. Otherweise the domain will be prepended to the redirect, thus allowing to use an external refresh server. See [javax.servlet.http.Cookie#setDomain(String)](https://docs.oracle.com/javaee/7/api/javax/servlet/http/Cookie.html#setDomain-java.lang.String-)|
| cookie.httpOnly            | true                | The cookie will not be accessible by client JavaScript if enabled (highly recommend)|
| cookie.secure              | true                | The cookie will only be sent over an encrypted (https) connection (recommend)|
| cookie.path                | /auth/refresh       | The issued access token cookie will only be sent by the client to URIs matching this pattern. This path spec has to include the endpoint for refreshing tokens, otherwise this won't work! See [javax.servlet.http.Cookie#setPath(String)](https://docs.oracle.com/javaee/7/api/javax/servlet/http/Cookie.html#setPath-java.lang.String-)|
| expiration                 | 1 day               | How long are refresh tokens valid? For details on how this field has to specified see [TimeWithPeriod](base/src/main/java/eu/fraho/spring/securityJwt/base/dto/TimeWithPeriod.java)|
| length                     | 24                  | Defines the length of refresh tokens in bytes, without the base64 encoding|
| path                       | /auth/refresh       | Sets the path for the RestController, defining the endpoint for refresh requests.|

## Other configuration properties

| Property              | Default      | Description   |
|-----------------------|--------------|---------------|
| fraho.jwt.logout.path | /auth/logout | Sets the path for the RestController, defining the endpoint for logging out. This path is only available if cookies are enabled.|
| fraho.totp.length     | 16           | Defines the length of the generated TOTP secrets|
| fraho.totp.variance   | 3            | Defines the allowed variance / validity of TOTP pins. The number defines how many "old / expired" pins will be considered valid. A value of "3" is the official suggestion for TOTP. This value is used to consider small clock-differences between the client and server.|
| fraho.crypt.algorithm | SHA512       | Configure the used crypt algorithm. For a list of possible values see [CryptAlgorithm](base/src/main/java/eu/fraho/spring/securityJwt/base/dto/CryptAlgorithm.java) Please be aware that changing this parameter has a major effect on the strength of the hashed password! Do not use insecure algorithms (as DES or MD5 as time of writing) unless you really know what you do!|
| fraho.crypt.rounds    | 10,000       | Defines the "strength" of the hashing function. The more rounds used, the more secure the generated hash. But beware that more rounds mean more cpu-load and longer computation times! This parameter is only used if the specified algorithm supports hashing rounds.|

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
* Code coverage should stay about the same level (please write tests for new features!)
* When writing new modules please use my abstract testclasses which provide a great base (
  see [internal](internal/src/test/java/eu/fraho/spring/securityJwt/internal) for an example)

# Releasing

Releasing is done with the default gradle tasks:

```bash
# to local repository:
./gradlew publishToMavenLocal
# to central:
./gradlew publish
```

# JWT Request Flow

## UML (with headers)

[![Sequence diagram](doc/headers.png)](doc/headers.png)

## UML (with cookies)

[![Sequence diagram](doc/cookies.png)](doc/cookies.png)

## HTTP-Requests

Request a token (login):

```
> POST /auth/login HTTP/1.1
> Host: localhost:8080
> Content-Type: application/json
> Content-Length: 63
>
> {
>   "username": "userA",
>   "password": "userA"
> }
>
< HTTP/1.1 200
< Content-Type: application/json;charset=UTF-8
< Content-Length: 491
<
< {
<   "accessToken": {
<     "token": "eyJhbGciOiJFUzI1NiJ9.<some more token code>",
<     "expiresIn": 3600,
<     "type": "Bearer"
<   },
<   "refreshToken": {
<     "token": "5bYNdXEGQRzz6xD4yFmw2wNPjXAh+wMc",
<     "expiresIn": 604800
<   }
< }
```

Usage of token to access some (secured) data:

```
> GET /users HTTP/1.1
> Authorization: Bearer eyJhbGciOiJFUzI1NiJ9...
>
< HTTP/1.1 200
< Content-Type: application/json;charset=UTF-8
< Content-Length: 42
<
< (some json content, result of request)
```

Use refresh token when token expired:

```
> POST /auth/refresh HTTP/1.1
> Content-Type: application/json
> Content-Length: 56
>
> {
>   "refreshToken": "5bYNdXEGQRzz6xD4yFmw2wNPjXAh+wMc"
> }
>
< HTTP/1.1 200
< Content-Type: application/json;charset=UTF-8
< Content-Length: 491
< Date: Fri, 19 May 2017 08:05:19 GMT
<
< {
<   "accessToken": {
<     "token": "eyJhbGciOiJFUzI1NiJ9.<some more token code>",
<     "expiresIn": 3600,
<     "type": "Bearer"
<   },
<   "refreshToken": {
<     "token": "U3LFL8dVZAeAp8Js6db2zrHGPfGslIeQ",
<     "expiresIn": 604800
<   }
< }
```
