/*
 * MIT Licence
 * Copyright (c) 2019 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
module fraho.securityJwt.filesStarter {
    requires static lombok;

    requires com.fasterxml.jackson.databind;
    requires fraho.securityJwt.base;
    requires fraho.securityJwt.baseStarter;
    requires fraho.securityJwt.files;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.security.core;

    exports eu.fraho.spring.securityJwt.files.starter;
}