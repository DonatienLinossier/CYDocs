// security/JwtTokenUtil.java
package com.project.auth.security;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {
    public String generateToken(String username) {
        return "token_for_" + username;
    }
}
