package com.olg.core.auth;

import com.olg.core.auth.dto.UserClaims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * To store the user's authentication details
 */
public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public CustomAuthenticationToken(UserClaims principal) {
        super(principal, null, AuthorityUtils.NO_AUTHORITIES); // No authorities by default
    }

}
