/*
 * MIT Licence
 * Copyright (c) 2019 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
module fraho.securityJwt.baseStarter {
    requires static lombok;

    requires fraho.securityJwt.base;
    requires org.bouncycastle.provider;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.security.config;
    requires spring.security.core;

    exports eu.fraho.spring.securityJwt.base.starter;
}