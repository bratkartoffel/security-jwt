/*
 * MIT Licence
 * Copyright (c) 2019 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
module fraho.securityJwt.files {
    requires static lombok;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires fraho.securityJwt.base;
    requires spring.beans;
    requires spring.boot;
    requires spring.context;
    requires spring.security.core;

    exports eu.fraho.spring.securityJwt.files.config;
    exports eu.fraho.spring.securityJwt.files.service;
}