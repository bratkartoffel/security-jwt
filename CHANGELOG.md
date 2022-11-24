# Changelog

### [unreleased]
* no changes yet

### [5.0.0] (2022-11-24)
* add support for spring boot 3 (breaking, _not_ backwards compatible)
* require java 17 (as of spring boot 3)

### [4.6.3] (2022-11-24)
* (all) upgrade jackson to 2.14.1
* (all) upgrade nimbus-jose-jwt to 9.25.6
* (all) upgrade openapi to 1.6.13
* (redis) upgrade jedis to 4.3.1

### [4.6.2] (2022-05-19)
* (all) upgrade jackson to 2.13.3
* (all) upgrade nimbus-jose-jwt to 9.22
* (all) upgrade openapi to 1.6.8
* (redis) upgrade jedis to 4.2.3

### [4.6.1] (2022-02-25)
* (all) remove runtime dependency on mockito (used for tests only of course)
* (all) upgrade nimbus-jose-jwt to 9.20

### [4.6.0] (2022-02-13)
* (base) extend CryptPasswordEncoder to also handle bcrypt ($2a) hashes

### [4.5.2] (2022-02-08)
* (all) upgrade openapi to 1.6.6
* (all) upgrade nimbus-jose-jwt to 9.19
* (redis) upgrade jedis to 4.1.1

### [4.5.1] (2022-01-25)
* (all) upgrade bouncycastle to 1.70
* (all) upgrade openapi to 1.6.5
* (all) upgrade jackson to 2.13.1
* (all) upgrade nimbus-jose-jwt to 9.16
* (redis) upgrade jedis to 4.0.1
* (base) remove custom JwtAuthenticationEntryPoint, use spring provided class

### [4.5.0] (2021-11-19)
* (all) make private methods in refresh tokenstore implementations protected to make the classes easier to extend
* (all) make compatible with spring bot 2.0+ again
* (all) upgrade bouncycastle to 1.69
* (all) upgrade nimbus-jose-jwt to 9.15.2
* (all) upgrade openapi to 1.5.12
* (hibernate) minor refactoring
* (internal) make the in-memory RefreshTokenMap accessible to subclasses
* (internal) upgrade epxiring map to 0.5.10
* (memcache) use specialized exception on timeouts instead of generic `RuntimeException`
* (redis) upgrade jedis to 3.7.0

### [4.4.2] (2021-05-06)
* (all) Dependencies upgraded to latest
* (all) Fixes "Found WebSecurityConfigurerAdapter as well as SecurityFilterChain"-error (#32)

### [4.4.1] (2020-11-08)
* (all) Dependencies upgraded to latest

### [4.4.0] (2020-07-15)
* (all) Migrate tests from Junit4 to Junit5
* (base) Some minor internal changes and optimizations
* (base) Remove springfox and integrate springdoc (OpenApi 3)

### [4.3.0] (2020-05-28)
* (all) Update dependencies
* (all) Refactoring of gradle build scripts
* (all) Remove module-info again (wasn't working correctly anyway)
* (memcache) Fix bug when running against memcached >= 1.5 which made this plugin effectively useless

### [4.2.0] (2020-02-08)
* (all) Update dependencies

### [4.1.1] (2019-09-21)
* (all) Update dependencies

### [4.1.0] (2019-03-29)
* (all) Cleanup gradle build scripts
* (all) Fix compile / build with Java 11 
* (all) Separate modules for usage (java 9+)
* (hibernate): Use IDENTITY as strategy for entity 

### [4.0.0] (2018-09-28)
* (redis) Add support for redis as backend
* (files) Add support for filesystem as backend
* (base) TimeWithPeriod is now serializable with Jackson
* (base) Split the all-mighty rest controller into separate ones and extract logic into services (which you may override now)
* (base) Refactoring of base to ease overriding specific behaviour

### [3.0.0] (2018-04-17)
* (all) Add support for spring boot 2
* (all) Set minimum required boot version to 2.x
* (base) Update nimbus library to at least 5.7
* (base) Add logout endpoint to clear cookies
* (base) Reworked TimeWithPeriod to support greater units than "days"
* (base) TokenProperties can now load the files "lazy" by changing the static field "loadEager" to false. This way you control when you load the keys by invoking the "loadKeys" method on the instance. An use case for this feature would be, if you would like to generate the jwt keys on the fly on startup.
* (base) TimeWithPeriod and expiresIn in Auth-Responses now use long instead of int
* (base) Lombok is no longer a runtime dependency

### [2.0.1] (2017-12-09)
* (all) Replaced beans constructor-based autowiring with setters
* (all) Project is now compatible with DCEVM and Hotswap agent

### [2.0.0] (2017-11-30)
* (base) Another great refactoring:
  * Remove deviceId from everywhere
    * Clients do no longer need any deviceId for refresh tokens
    * Getting new tokens with refresh now works solely with refreshToken
  * Remove ```fraho.jwt.refresh.delimiter```, not needed any more
  * Remove deprecated JwtTokenService.getExpiration
  * Remove deprecated RefreshTokenStore.getRefreshExpiration
  * Reworked RefreshTokenStore interface (use JwtUser instance instead of username)
  * Reworked JwtTokenService (use JwtUser instance instead of username)
  * All modules now have their own package
    * e.g. internal moved from ```eu.fraho.spring.securityJwt.service.InternalTokenStore``` to ```eu.fraho.spring.securityJwt.service.internal.InternalTokenStore```
* (base) Add support for tokens sent as cookies (both access and refresh)
* (base) Make JwtTokenService.getToken() deprecated

### [1.1.0] (2017-11-30)
* (base) Make JwtUser extendable (#20)
* (base) Make rest endpoints configurable (#19)
* (base) Mark JwtTokenService.getExpiration as deprecated
* (base) Mark RefreshTokenStore.getRefreshExpiration as deprecated
* (doc) Create a plantuml sequence diagram for README

### [1.0.0] (2017-09-05)
* (all) Add spring configuration properties support for IDE auto-completion
* (all) Renamed / moved some properties
  * ```fraho.jwt.refresh.cache.impl``` to ```fraho.jwt.refresh.cache-impl```
  * ```fraho.jwt.refresh.cache.prefix``` to ```fraho.jwt.refresh.memcache.prefix```
* (all) Provide configuration classes, enables content assis when writing appplication.yaml
* (base) Remove deprecated JwtUser.setTotpSecret(Optional)
* (base) Add log output with running library version on startup
* (all) Add spring boot starter projects
* (base) When using HMAC-based signatures and no keyfile was specified, then create a new (in memory) random one instead of raising an exception
* (base) Change the default value for ```fraho.jwt.token.algorithm``` to ```HS256```
* (base) Rename ```WebSecurityConfig``` to ```JwtSecurityConfig```
* (base) Delete ```JwtAuthenticationEntryPoint```

### [0.8.1] (2017-08-17)
* (all) Some minor cleanup (code smells)
* (all) Add Jetbrains Annotations for Nullable and NotNull constraints
* (all) Prefer constructor based autowiring over field injection
* (base) Fix NPE in PasswordEncoder
* (base) Use swagger-annotations instead of full springfox (#6)
* (doc) Add example schema to hibernate doc
* (doc) Add UML showing the graphical flow of requests (Thanks to Hans-Peter Keilhofer)
* (hibernate) Ensure that "created" is a timestamp column
* (hibernate) Do not register DateTime-Converter globally

### [0.8.0] (2017-06-06)
* (base) Fix JwtTokenService.isRefreshTokenSupported() not working as expected (always returning true)
* (doc) Add a changelog file (#13)
* (base) Support multiple roles for JwtUser (#2)
* (memcache, internal) Delimiter for map keys are now configurable (#14)

### [0.7.0] (2017-06-02)
* (hibernate) Add hibernate module (Support storage of refresh tokens in a jdbc database)
* (base) Add jackson java8 module to compile path (#1)
* (base) Add Insomnia project as an example on how to interact with login / refresh
* (test) Huge refactoring of testcode, removed a lot of redundancy
* (test)First publication of abstract testclases for other modules

### [0.6.0] (2017-05-19)
* Somehow messed up my git repository, so this release will be the base for all further releases
* Many untracked changes (sorry)

### [0.5.2] (2017-05-17)
* Initial release to github and maven central


[unreleased]: https://github.com/bratkartoffel/security-jwt/compare/5.0.0...develop
[5.0.0]: https://github.com/bratkartoffel/security-jwt/compare/4.6.3...5.0.0
[4.6.3]: https://github.com/bratkartoffel/security-jwt/compare/4.6.2...4.6.3
[4.6.2]: https://github.com/bratkartoffel/security-jwt/compare/4.6.1...4.6.2
[4.6.1]: https://github.com/bratkartoffel/security-jwt/compare/4.6.0...4.6.1
[4.6.0]: https://github.com/bratkartoffel/security-jwt/compare/4.5.2...4.6.0
[4.5.2]: https://github.com/bratkartoffel/security-jwt/compare/4.5.1...4.5.2
[4.5.1]: https://github.com/bratkartoffel/security-jwt/compare/4.5.0...4.5.1
[4.5.0]: https://github.com/bratkartoffel/security-jwt/compare/4.4.2...4.5.0
[4.4.2]: https://github.com/bratkartoffel/security-jwt/compare/4.4.1...4.4.2
[4.4.1]: https://github.com/bratkartoffel/security-jwt/compare/4.4.0...4.4.1
[4.4.0]: https://github.com/bratkartoffel/security-jwt/compare/4.3.0...4.4.0
[4.3.0]: https://github.com/bratkartoffel/security-jwt/compare/4.2.0...4.3.0
[4.2.0]: https://github.com/bratkartoffel/security-jwt/compare/4.1.1...4.2.0
[4.1.1]: https://github.com/bratkartoffel/security-jwt/compare/4.1.0...4.1.1
[4.1.0]: https://github.com/bratkartoffel/security-jwt/compare/4.0.0...4.1.0
[4.0.0]: https://github.com/bratkartoffel/security-jwt/compare/3.0.0...4.0.0
[3.0.0]: https://github.com/bratkartoffel/security-jwt/compare/2.0.1...3.0.0
[2.0.1]: https://github.com/bratkartoffel/security-jwt/compare/2.0.0...2.0.1
[2.0.0]: https://github.com/bratkartoffel/security-jwt/compare/1.1.0...2.0.0
[1.1.0]: https://github.com/bratkartoffel/security-jwt/compare/1.0.0...1.1.0
[1.0.0]: https://github.com/bratkartoffel/security-jwt/compare/0.8.1...1.0.0
[0.8.1]: https://github.com/bratkartoffel/security-jwt/compare/0.8.0...0.8.1
[0.8.0]: https://github.com/bratkartoffel/security-jwt/compare/0.7.0...0.8.0
[0.7.0]: https://github.com/bratkartoffel/security-jwt/compare/0.6.0...0.7.0
[0.6.0]: https://github.com/bratkartoffel/security-jwt/compare/0.5.2...0.6.0
[0.5.2]: https://github.com/bratkartoffel/security-jwt/tree/0.5.2
