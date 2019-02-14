/*
 * MIT Licence
 * Copyright (c) 2019 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
module fraho.securityJwt.redisStarter {
    requires static lombok;

    requires fraho.securityJwt.base;
    requires fraho.securityJwt.baseStarter;
    requires fraho.securityJwt.redis;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.security.core;

    exports eu.fraho.spring.securityJwt.redis.starter;
}