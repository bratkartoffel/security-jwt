/*
 * MIT Licence
 * Copyright (c) 2019 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
module fraho.securityJwt.hibernate {
    requires static lombok;

    requires fraho.securityJwt.base;
    requires java.persistence;
    requires java.sql;
    requires spring.beans;
    requires spring.security.core;
    requires spring.tx;

    exports eu.fraho.spring.securityJwt.hibernate.dto;
    exports eu.fraho.spring.securityJwt.hibernate.service;
}