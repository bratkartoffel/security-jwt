module fraho.securityJwt.base {
    requires static lombok;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires jcip.annotations;
    requires nimbus.jose.jwt;
    requires org.apache.commons.codec;
    requires org.slf4j;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.context;
    requires spring.core;
    requires spring.security.config;
    requires spring.security.core;
    requires spring.security.web;
    requires spring.web;
    requires swagger.annotations;

    exports eu.fraho.spring.securityJwt.base;
    exports eu.fraho.spring.securityJwt.base.config;
    exports eu.fraho.spring.securityJwt.base.controller;
    exports eu.fraho.spring.securityJwt.base.dto;
    exports eu.fraho.spring.securityJwt.base.exceptions;
    exports eu.fraho.spring.securityJwt.base.password;
    exports eu.fraho.spring.securityJwt.base.service;
}