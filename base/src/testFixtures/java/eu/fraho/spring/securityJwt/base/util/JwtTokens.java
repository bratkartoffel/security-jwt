/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.util;

public final class JwtTokens {
    public static final String FUTURE_IAT =
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb28iLCJ1aWQiOi0xLCJuYmYiOjEwMDQxODAwOTUsImlzcyI6ImZyYWhvLXNlY3VyaXR5IiwiZXhwIjozNTA0MTgzNjk1LCJpYXQiOjM1MDQxODM2OTQsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJqdGkiOiI4M2UwNDA1OS04ODhhLTRlZDMtOGE0ZS01NDcyZDExYmM5NzcifQ.EFdMv6ovnyIYmvivt2w2gqVnxflndio8ItXWHg1SGtk";

    public static final String FUTURE_NBF =
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb28iLCJ1aWQiOi0xLCJuYmYiOjM1MDQxODM2OTQsImlzcyI6ImZyYWhvLXNlY3VyaXR5IiwiZXhwIjozNTA0MTgzNjk1LCJpYXQiOjEwMDQxODAwOTUsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJqdGkiOiI4M2UwNDA1OS04ODhhLTRlZDMtOGE0ZS01NDcyZDExYmM5NzcifQ.H_H3CmWotBDYqx5xQF6rcU6gm3yQgmnpLNgI-52jtjg";

    public static final String INVALID_BODY =
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb28iLCJ1aWQiOi0xLCJuYmYiOjE1MDQyNjgwODUsImlzcyI6ImZyYWhvLXNlY3VyaXR5IiwiZXhwIjoyOTI0OTg4Mzk5LCJpYXQiOjE1MDQyNjgwODUsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJqdGkiOiI4M2UwNDA1OS04ODhhLTRlZDMOGE0ZS01NDcyZDExYmM5NzcifQ.R8d0YptUondL-JF2R1il6ghxIQ9CwH-GIcZqdvqpXxM";

    public static final String INVALID_SIGNATURE =
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb28iLCJ1aWQiOi0xLCJuYmYiOjEwMDQxODAwOTUsImlzcyI6ImZyYWhvLXNlY3VyaXR5IiwiZXhwIjozNTA0MTgzNjk1LCJpYXQiOjEwMDQxODAwOTUsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJqdGkiOiI4M2UwNDA1OS04ODhhLTRlZDMtOGE0ZS01NDcyZDExYmM5NzcifQ.7dJCYc8F87WZSpNqPmtAcWv9917zHS58Mx0G022UnRw";

    public static final String NO_EXP =
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb28iLCJ1aWQiOi0xLCJuYmYiOjEwMDQxODAwOTUsImlzcyI6ImZyYWhvLXNlY3VyaXR5IiwiaWF0IjoxMDA0MTgwMDk1LCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiODNlMDQwNTktODg4YS00ZWQzLThhNGUtNTQ3MmQxMWJjOTc3In0.muUCzolu1sZkYjzgXYQKN7dqd6SB3tj67l54B8rFwH4";

    public static final String NO_IAT =
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb28iLCJ1aWQiOi0xLCJuYmYiOjEwMDQxODAwOTUsImlzcyI6ImZyYWhvLXNlY3VyaXR5IiwiZXhwIjozNTA0MTgzNjk1LCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiODNlMDQwNTktODg4YS00ZWQzLThhNGUtNTQ3MmQxMWJjOTc3In0.kO-1RtRnJN4ctSf2nGm-UAQFHV-Ny5QY6ukjPLBp8jk";

    public static final String NO_NBF =
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb28iLCJ1aWQiOi0xLCJpc3MiOiJmcmFoby1zZWN1cml0eSIsImV4cCI6MzUwNDE4MzY5NSwiaWF0IjoxMDA0MTgwMDk1LCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiODNlMDQwNTktODg4YS00ZWQzLThhNGUtNTQ3MmQxMWJjOTc3In0.vbGpgpOqQmhXizwWLoJH3qA_aCRJXfuRJHplOBDG6nk";

    public static final String VALID =
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb28iLCJ1aWQiOi0xLCJuYmYiOjE1MDQyNjgwODUsImlzcyI6ImZyYWhvLXNlY3VyaXR5IiwiZXhwIjoyOTI0OTg4Mzk5LCJpYXQiOjE1MDQyNjgwODUsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJqdGkiOiI4M2UwNDA1OS04ODhhLTRlZDMtOGE0ZS01NDcyZDExYmM5NzcifQ.R8d0YptUondL-JF2R1il6ghxIQ9CwH-GIcZqdvqpXxM";

    private JwtTokens() {
        super();
    }
}
