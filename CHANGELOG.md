# Changelog

### 1.0.0 (not yet released)
* (all) Add spring configuration properties support for IDE auto-completion
* (all) Renamed / moved some properties
  * ```fraho.jwt.refresh.cache.impl``` => ```fraho.jwt.refresh.cache-impl```
  * ```fraho.jwt.refresh.cache.prefix``` => ```fraho.jwt.refresh.memcache.prefix```
* (all) Provide configuration classes, enables content assis when writing appplication.yaml
* (base) Remove deprecated JwtUser.setTotpSecret(Optional)
* (base) Add log output with running library version on startup

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
* (hibernate) Add [hibernate](hibernate/) module (Support storage of refresh tokens in a jdbc database)
* (base) Add jackson java8 module to compile path (#1)
* (base) Add [Insomnia](https://insomnia.rest/) project as an example on how to interact with login / refresh
* (test) Huge refactoring of testcode, removed a lot of redundancy
* (test)First publication of abstract testclases for other modules

### 0.6.0 (2017-05-19)
* Somehow messed up my git repository, so this release will be the base for all further releases
* Many untracked changes (sorry)

### 0.5.2 (2017-05-17)
* Initial release to github and maven central
