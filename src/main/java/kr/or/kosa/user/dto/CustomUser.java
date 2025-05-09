package kr.or.kosa.user.dto;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUser implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2189135186L;
	private final Users user;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return user.getAuthList().stream().map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
				.collect(Collectors.toList());
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	public String getNickname() {
		return user.getNickname();
	}

	public String getProfileImg() {
		return user.getProfileImg();
	}

	public Long getUserId() {
		return user.getUserId();
	}
}
