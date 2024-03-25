package com.example.springSecurity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain filterChanin(HttpSecurity http) throws Exception {
		http.csrf(auth -> auth.disable())					// 괄호 안에 람다함수를 사용해야 함
			.headers(x -> x.frameOptions(y -> y.disable()))			// CK Editor image upload
			.authorizeHttpRequests(auth -> auth
					.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
					.requestMatchers("/user/login", "/user/logout", "/user/register",
							"/img/**", "/css/**", "/js/**", "/error/**").permitAll()
					.requestMatchers("/admin/**").hasAuthority("ADMIN")
					.anyRequest().authenticated()
				)
				.formLogin(auth -> auth
						.loginPage("/user/login")				// 로그인 폼
						.loginProcessingUrl("/user/login")		// 스프링 시큐리티가 만듬. UserDetailsService 구현객체에서 처리해줘야 함
						.usernameParameter("uid")
						.passwordParameter("pwd")
						.defaultSuccessUrl("/user/loginSuccess", true)		// 내가 로그인 후 해야할 일, ex) 세션 세팅, '오늘의 메시지' 등등
						.permitAll()
				)
			;
		
		return http.build();
	}
}