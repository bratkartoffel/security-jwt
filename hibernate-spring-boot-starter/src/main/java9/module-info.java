/*
 * MIT Licence
 * Copyright (c) 2019 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
module fraho.securityJwt.hibernateStarter {
    requires static lombok;

    requires fraho.securityJwt.base;
    requires fraho.securityJwt.baseStarter;
    requires fraho.securityJwt.hibernate;
    requires java.persistence;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.security.core;

    exports eu.fraho.spring.securityJwt.hibernate.starter;
}