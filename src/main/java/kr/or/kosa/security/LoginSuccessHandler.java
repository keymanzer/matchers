package kr.or.kosa.security;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		String redirectUrl = "/";

		for (GrantedAuthority auth : authorities) {
			if (auth.getAuthority().equals("ROLE_ADMIN")) {
				redirectUrl = "/admin";
				break;
			}
		}

		response.sendRedirect(redirectUrl);
	}

}
