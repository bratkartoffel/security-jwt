/*
 * MIT Licence
 * Copyright (c) 2019 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
module fraho.securityJwt.memcache {
    requires static lombok;

    requires fraho.securityJwt.base;
    requires jcip.annotations;
    requires spring.beans;
    requires spring.boot;
    requires spring.context;
    requires spring.security.core;
    requires spymemcached;

    exports eu.fraho.spring.securityJwt.memcache.config;
    exports eu.fraho.spring.securityJwt.memcache.service;
}