package com.EmployeeManagement.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {
	private final CorsConfigurationSource corsConfigurationSource;	
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource, 
                          JwtAuthenticationFilter jwtAuthFilter,
                          CustomUserDetailsService customUserDetailsService) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.jwtAuthFilter = jwtAuthFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/assets/**", "/static/**");
    }

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .cors(cors -> cors.configurationSource(corsConfigurationSource))
	        .csrf(csrf -> csrf.disable())
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
	            .requestMatchers("/uploads/**").permitAll()
	            .requestMatchers("/", "/api/auth/**").permitAll()
	            .requestMatchers(HttpMethod.GET, "/api/employees/**").permitAll()
	            .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasAnyRole("USER", "EMPLOYEE", "ADMIN")
	            .requestMatchers(HttpMethod.POST, "/api/employees/*/upload-image").hasAnyRole("USER", "EMPLOYEE", "ADMIN")
	            .requestMatchers("/api/employees/**").hasRole("ADMIN")
	            .requestMatchers(HttpMethod.GET, "/api/company", "/api/company/**").permitAll()
	            .requestMatchers(HttpMethod.POST, "/api/company", "/api/company/**").hasAnyRole("EMPLOYEE", "ADMIN")
	            .requestMatchers("/api/company/**").hasRole("ADMIN")
	            .requestMatchers(HttpMethod.GET, "/api/admin/assets/{id}").hasAnyRole("EMPLOYEE", "ADMIN")
	            .requestMatchers("/api/admin/assets/employee/**").hasAnyRole( "EMPLOYEE", "ADMIN")
	            .requestMatchers("/api/admin/assets/**").hasRole("ADMIN")
	            .requestMatchers("/api/careers/**").permitAll()
	            .requestMatchers("/api/leaves/**").authenticated()
	            .requestMatchers("/api/careers/applications/employee/**")
	            .hasAnyRole("EMPLOYEE", "ADMIN")
	            .requestMatchers(HttpMethod.GET, "/api/shifts/**").authenticated()
	            .requestMatchers("/api/shifts/**").hasRole("ADMIN")
	            .requestMatchers("/api/shift-assign/employee/**").authenticated()
	            .requestMatchers("/api/shift-assign/**").hasRole("ADMIN")
	            .requestMatchers("/api/attendance/check-in", "/api/attendance/check-out").authenticated()
	            .requestMatchers("/api/attendance/employee/**").authenticated()
	            .requestMatchers("/api/attendance/**").hasRole("ADMIN")
	            .anyRequest().authenticated()
	        )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}