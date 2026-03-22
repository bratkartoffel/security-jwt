/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.it.spring;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SecuredController {
    @RequestMapping("/user")
    @Secured("ROLE_USER")
    public ResponseEntity<String> testUser() {
        return ResponseEntity.ok("Hello world!");
    }

    @RequestMapping("/admin")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<String> testAdmin() {
        return ResponseEntity.ok("Hello world!");
    }

    @RequestMapping("/authentication")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> authentication(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok("null token");
        } else if (authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.ok("anonymous token");
        } else {
            return ResponseEntity.ok("authenticated token");
        }
    }
}
