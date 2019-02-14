/*
 * MIT Licence
 * Copyright (c) 2019 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
module fraho.securityJwt.redis {
    requires static lombok;

    requires fraho.securityJwt.base;
    requires jcip.annotations;
    requires jedis;
    requires spring.beans;
    requires spring.boot;
    requires spring.context;
    requires spring.security.core;

    exports eu.fraho.spring.securityJwt.redis.config;
    exports eu.fraho.spring.securityJwt.redis.service;
}