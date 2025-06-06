package kr.or.kosa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import kr.or.kosa.security.CustomAccessDeniedHandler;
import kr.or.kosa.security.CustomDetailService;
import kr.or.kosa.security.CustomOAuth2UserService;
import kr.or.kosa.security.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomDetailService customDetailService;

	private final CustomOAuth2UserService customOAuth2UserService;

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/login", "/signup", "/check-email", "/reset-password/**", "/loginPro", "/css/**",
						"/js/**", "/images/**", "/uploads/**", "/oauth2/**", "/connect", "/sse/**")
				.permitAll().requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/user/**", "/chat/**")
				.hasAnyRole("USER", "ADMIN").anyRequest().authenticated());

		http.oauth2Login(oauth2 -> oauth2.loginPage("/login")
				.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
				.successHandler(authenticationSuccessHandler()));

		http.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").deleteCookies("JSESSIONID")
				.invalidateHttpSession(true));

		http.formLogin(form -> form.loginPage("/login").loginProcessingUrl("/loginPro").usernameParameter("id")
				.passwordParameter("pw").successHandler(authenticationSuccessHandler()).permitAll());

		http.userDetailsService(customDetailService);
		http.exceptionHandling(exceptions -> exceptions.accessDeniedHandler(accessDeniedHandler()));

		return http.build();
	}

	@Bean
	AccessDeniedHandler accessDeniedHandler() {
		return new CustomAccessDeniedHandler();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationSuccessHandler authenticationSuccessHandler() {
		return new LoginSuccessHandler();
	}
}