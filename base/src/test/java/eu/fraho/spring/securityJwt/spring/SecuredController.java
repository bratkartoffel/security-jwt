/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.spring;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SecuredController {
    @RequestMapping("/user")
    @Secured("ROLE_USER")
    public ResponseEntity testUser() {
        return ResponseEntity.ok("Hello world!");
    }

    @RequestMapping("/admin")
    @Secured("ROLE_ADMIN")
    public ResponseEntity testAdmin() {
        return ResponseEntity.ok("Hello world!");
    }
}
