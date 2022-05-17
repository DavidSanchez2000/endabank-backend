package com.endava.endabank.configuration;

import com.endava.endabank.constants.Routes;
import com.endava.endabank.security.jwtauthentication.JwtAuthenticationEntryPoint;
import com.endava.endabank.security.jwtauthentication.JwtRequestFilter;
import com.endava.endabank.service.impl.UserAuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AllArgsConstructor
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final JwtRequestFilter jwtRequestFilter;
    private final UserAuthenticationService userAuthentication;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userAuthentication).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable().cors().and()
                .authorizeRequests().antMatchers(HttpMethod.POST, Routes.API_LOGIN_ROUTE,
                        Routes.API_USERS_ROUTE, Routes.API_USERS_ROUTE + Routes.EMAIL_VALIDATION_ROUTE + "/**").permitAll();
        httpSecurity.authorizeRequests().antMatchers(
                HttpMethod.GET, Routes.API_USERS_ROUTE +
                        Routes.RESET_PASSWORD_ROUTE + "/**", Routes.SWAGGER_IU,
                Routes.SWAGGER_JSON, Routes.CONFIGURATION,
                Routes.SWAGGER, Routes.WEB_JARS, Routes.API_USERS_ROUTE +
                        Routes.EMAIL_VALIDATION_ROUTE + "/**").permitAll();
        httpSecurity.authorizeRequests().antMatchers(
                HttpMethod.PUT, Routes.API_ROUTE +
                        Routes.USERS_ROUTE + Routes.RESET_PASSWORD_ROUTE + "/**").permitAll();
        httpSecurity.authorizeRequests().anyRequest().authenticated().and()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
