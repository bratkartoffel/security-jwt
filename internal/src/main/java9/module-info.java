/*
 * MIT Licence
 * Copyright (c) 2019 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
module fraho.securityJwt.internal {
    requires static lombok;

    requires expiringmap;
    requires fraho.securityJwt.base;
    requires spring.beans;
    requires spring.security.core;

    exports eu.fraho.spring.securityJwt.internal.service;
}