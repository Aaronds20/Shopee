package com.shopee.shopee.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.shopee.shopee.daos.UserRepository;
import com.shopee.shopee.entities.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {

    @Autowired
	private UserRepository userRepo;
	
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	@Bean
	public UserDetailsServiceImpl getUserDetailsService() {
		return new UserDetailsServiceImpl();
	}
    
    @Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.getUserDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}
	

	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
               auth.authenticationProvider(authenticationProvider());
	}
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests((auth) -> auth
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/customer/**").hasRole("CUSTOMER")
            .requestMatchers("/seller/**").hasRole("SELLER")
            .requestMatchers("/**").permitAll()
            .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				
				User user = userRepo.loadUserByUserName(authentication.getName());
				
				String redirectURL = request.getContextPath();
				
				if(user.getRole().equals("ROLE_CUSTOMER")) {
					redirectURL = "/customer/home";
				}
				if(user.getRole().equals("ROLE_SELLER")) {
					redirectURL = "/seller/home";
				}
				if(user.getRole().equals("ROLE_ADMIN")) {
					redirectURL = "/admin/home";
				}
				
				response.sendRedirect(redirectURL);
                }})
            .failureHandler(new AuthenticationFailureHandler(){
                @Override
                public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException exception) throws IOException, ServletException {
                            HttpSession httpSession=request.getSession();
                            if (exception.getMessage().equals("Bad credentials")) {
                               httpSession.setAttribute("status", "bad-credentials");
                                response.sendRedirect("/login?=BadCredentials");
                                return;
                            }
                            if(exception.getMessage().equals("User is disabled")) {
                                httpSession.setAttribute("status", "user-disabled");
                                response.sendRedirect("/login?=AccountSuspended");
                            }
                }
                
            }     
            )
            .permitAll())
            .logout(logout -> logout
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessHandler(new LogoutSuccessHandler(){

                @Override
                public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) throws IOException, ServletException {
                            HttpSession httpSession = request.getSession();
				
                            if(authentication!=null) {
                                httpSession.setAttribute("status", "logout-success");
                                response.sendRedirect("/login?logoutSuccess");
                            }
                            else {
                                response.sendRedirect("/login?doLogin");
                            }
                }

            })
            .permitAll()
            );
        return http.build();
    }
    
}
