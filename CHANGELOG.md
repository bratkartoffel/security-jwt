# Changelog

### 2.0.0 (not yet released)
* (base) Another great refactoring:
  * Remove deviceId from everywhere
    * Clients do no longer need any deviceId for refresh tokens
    * Getting new tokens with refresh now works solely with refreshToken
  * Remove ```fraho.jwt.refresh.delimiter```, not needed any more
  * Remove deprecated JwtTokenService.getExpiration
  * Remove deprecated RefreshTokenStore.getRefreshExpiration
  * Reworked RefreshTokenStore interface (use JwtUser instance instead of username)
  * Reworked JwtTokenService (use JwtUser instance instead of username)
* (base) Add support for tokens sent as cookies (both access and refresh)

### 1.1.0 (not yet released)
* (base) Make JwtUser extendable (#20)
* (base) Make rest endpoints configurable (#19)
* (base) Mark JwtTokenService.getExpiration as deprecated
* (base) Mark RefreshTokenStore.getRefreshExpiration as deprecated
* (doc) Create a plantuml sequence diagram for README

### 1.0.0 (2017-09-05)
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

### 0.8.1 (2017-08-17)
* (all) Some minor cleanup (code smells)
* (all) Add Jetbrains Annotations for Nullable and NotNull constraints
* (all) Prefer constructor based autowiring over field injection
* (base) Fix NPE in PasswordEncoder
* (base) Use swagger-annotations instead of full springfox (#6)
* (doc) Add example schema to hibernate doc
* (doc) Add UML showing the graphical flow of requests (Thanks to Hans-Peter Keilhofer)
* (hibernate) Ensure that "created" is a timestamp column
* (hibernate) Do not register DateTime-Converter globally

### 0.8.0 (2017-06-06)
* (base) Fix JwtTokenService.isRefreshTokenSupported() not working as expected (always returning true)
* (doc) Add a changelog file (#13)
* (base) Support multiple roles for JwtUser (#2)
* (memcache, internal) Delimiter for map keys are now configurable (#14)

### 0.7.0 (2017-06-02)
* (hibernate) Add hibernate module (Support storage of refresh tokens in a jdbc database)
* (base) Add jackson java8 module to compile path (#1)
* (base) Add Insomnia project as an example on how to interact with login / refresh
* (test) Huge refactoring of testcode, removed a lot of redundancy
* (test)First publication of abstract testclases for other modules

### 0.6.0 (2017-05-19)
* Somehow messed up my git repository, so this release will be the base for all further releases
* Many untracked changes (sorry)

### 0.5.2 (2017-05-17)
* Initial release to github and maven central
