

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
package com.osttra.config;

 

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.Map;

 

import javax.servlet.http.HttpServletResponse;

 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.CustomAutowireConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osttra.helper.AuthEntryPointJwt;
import com.osttra.service.CustomUserDetailsService;

 

 

@Configuration
@EnableMethodSecurity
//(securedEnabled = true,
//jsr250Enabled = true,
//prePostEnabled = true) // by default
public class SecurityConfig { 
  @Autowired
  CustomUserDetailsService userDetailsService;

  private static final Logger LOGGER = Logger.getLogger(SecurityConfig.class.getName());

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;
  

 

  @Bean
  public JWTAuthenticationFilter authenticationJwtTokenFilter() {
    return new JWTAuthenticationFilter();
  }

 


  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
      DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

      authProvider.setUserDetailsService(userDetailsService);
      authProvider.setPasswordEncoder(passwordEncoder());
      
      LOGGER.info("Authentication provider initialized.");

      return authProvider;
  }

 

  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
      return (request, response, e) -> {
          response.setContentType("application/json");
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);

          // Customize the JSON response as needed
          Map<String, String> responseBody = new HashMap<>();
          responseBody.put("message", "Access denied. You do not have the required role to access this resource.");

          LOGGER.warning("Access denied: " + e.getMessage());

          ObjectMapper objectMapper = new ObjectMapper();
          objectMapper.writeValue(response.getWriter(), responseBody);
      };
  }



  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

 

@Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }




  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
    .exceptionHandling(exception -> exception
    	    .authenticationEntryPoint(unauthorizedHandler)
    	    .accessDeniedHandler(accessDeniedHandler())
    	)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> 
          auth.antMatchers("/api/auth/**").permitAll()
              .antMatchers("/api/test/**").permitAll()
              .antMatchers("/actuator").permitAll()
              .antMatchers("/signin").permitAll()
              .antMatchers("/users/**").permitAll()
              .antMatchers("/usergroups/**").permitAll()
              .antMatchers("/history/**").permitAll()
              .antMatchers("/api/**").permitAll()

              .anyRequest().authenticated()
        );


    http.authenticationProvider(authenticationProvider());

 

    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    
    LOGGER.info("Security filter chain configured.");


    return http.build();
  }
}

 

 



 

