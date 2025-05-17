To add **JWT token validation** to protect your endpoints, we need to implement a **filter** in Spring Security. This filter will check the JWT token in the request headers, validate it, and set the authentication in the security context if the token is valid.

### 🛠️ Steps to Implement Token Validation Filter:

1. **Create a JWT Filter**: The filter will extract the JWT token from the `Authorization` header, validate it, and set the authentication in the Spring Security context.
2. **Configure Spring Security**: Update your `SecurityConfig` to register the filter in the filter chain.
3. **Create a Token Validation Service**: The service will validate the JWT token and extract the user's details.

---

### 1. **Create the JWT Token Validation Filter**

First, create a filter class that will handle the JWT validation.

```java
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Get the Authorization header from the request
        String authorizationHeader = request.getHeader("Authorization");

        // If there is a token in the Authorization header, validate it
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix

            try {
                if (jwtService.validateToken(token)) {
                    String username = jwtService.extractUsername(token);

                    // Set authentication in the security context
                    CustomAuthenticationToken authentication = new CustomAuthenticationToken(username);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                logger.error("JWT token validation failed", e);
            }
        }

        // Continue with the request
        filterChain.doFilter(request, response);
    }
}
```

### Explanation:
- **doFilterInternal**: This method gets the token from the `Authorization` header. If it's a valid token, it sets the authentication in the `SecurityContextHolder`.
- **CustomAuthenticationToken**: This is a custom implementation of `Authentication` that represents the user's authentication status.

### 2. **Token Validation Service**

In the `JwtService`, we need to implement the validation logic for the JWT token.

```java
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String SECRET_KEY = "your-secret-key";

    public String generateToken(String username) {
        // Generate a JWT token with a claim for the username
        return Jwts.builder()
                .setSubject(username)
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token);
            return true; // Token is valid
        } catch (Exception e) {
            return false; // Token is invalid
        }
    }

    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
```

### Explanation:
- **validateToken**: Verifies the token is signed correctly and hasn't expired.
- **extractUsername**: Extracts the username from the token.

### 3. **Create Custom Authentication Token**

To store the user's authentication details, you can create a custom `Authentication` token:

```java
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public CustomAuthenticationToken(String principal) {
        super(principal, null, AuthorityUtils.NO_AUTHORITIES); // No authorities by default
    }
}
```

### 4. **Configure Spring Security**

Now, you need to register the `JwtAuthenticationFilter` in your Spring Security configuration.

#### **SecurityConfig**

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Add the JwtAuthenticationFilter to the security filter chain
        http.addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
            .authorizeRequests()
            .antMatchers("/public/**").permitAll()  // Allow unauthenticated access to some paths
            .anyRequest().authenticated();  // Secure other paths
    }
}
```

### Explanation:
- **addFilterBefore**: Adds the `JwtAuthenticationFilter` before the `UsernamePasswordAuthenticationFilter`, which is the default filter in Spring Security. This ensures the JWT validation happens before any other authentication mechanisms.
- **antMatchers("/public/**").permitAll()**: This allows public access to the specified path (`/public/**`), while other paths require authentication.

---

### 5. **Testing the Filter**

To ensure everything works, you can test the endpoints by sending requests with a valid JWT token in the `Authorization` header.

Example of a request with JWT:

```http
GET /api/protected
Authorization: Bearer your-jwt-token
```

If the JWT is valid, the user will be authenticated and allowed to access protected endpoints. Otherwise, you'll get an **Unauthorized (401)** response.

---

### Conclusion:

You now have a basic **JWT token validation filter** integrated with Spring Security. This will validate the JWT token in the request and set the user’s authentication context. Let me know if you need further help with other parts of your project!