package kr.or.kosa.user.dto;

import java.sql.Date;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUser implements UserDetails, OAuth2User {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2189135186L;
	private final Users user;
	private Map<String, Object> attributes;

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

	public Date getUpdatedAt() {
		return user.getUpdatedAt();
	}

	@Override
	public String getName() {
		return user.getEmail();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
