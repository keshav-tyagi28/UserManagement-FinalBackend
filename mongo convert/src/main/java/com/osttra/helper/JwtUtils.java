package com.osttra.helper;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.osttra.to.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
//  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
//
//  @Value("${bezkoder.app.jwtSecret}")
//  private String jwtSecret;
//
//  @Value("${bezkoder.app.jwtExpirationMs}")
//  private int jwtExpirationMs;
//
//  public String generateJwtToken(Authentication authentication) {
//
//    //UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
//	  
//	  CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
//
//    return Jwts.builder()
//        .setSubject((userPrincipal.getUsername()))
//        .setIssuedAt(new Date())
//        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
//        .signWith(key(), SignatureAlgorithm.HS256)
//        .compact();
//  }
//  
//  private Key key() {
//    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
//  }
//
//  public String getUserNameFromJwtToken(String token) {
//    return Jwts.parserBuilder().setSigningKey(key()).build()
//               .parseClaimsJws(token).getBody().getSubject();
//  }
//
//  public boolean validateJwtToken(String authToken) {
//    try {
//      Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
//      return true;
//    } catch (MalformedJwtException e) {
//      logger.error("Invalid JWT token: {}", e.getMessage());
//    } catch (ExpiredJwtException e) {
//      logger.error("JWT token is expired: {}", e.getMessage());
//    } catch (UnsupportedJwtException e) {
//      logger.error("JWT token is unsupported: {}", e.getMessage());
//    } catch (IllegalArgumentException e) {
//      logger.error("JWT claims string is empty: {}", e.getMessage());
//    }
//
//    return false;
//  }
	
	
	//private String SECRET_KEY = "ThisIsASecretKeyForJWTAuthenticationhhhhhhhhhtdytcytcycyccytctcyccccccccccccccccttttttttrrrrrrrrrrrrrrrrr";

	private String SECRET_KEY="gJ73nQqT1Ziw7EGFlG6K7hR9a0P4D0M4lC6L3J8J1A4nX0H3eN6K6Q3qT4iW";
	
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
	
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
