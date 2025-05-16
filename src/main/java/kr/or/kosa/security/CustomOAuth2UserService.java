package kr.or.kosa.security;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kosa.mapper.UserMapper;
import kr.or.kosa.user.dto.CustomUser;
import kr.or.kosa.user.dto.UserAuth;
import kr.or.kosa.user.dto.Users;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserMapper userMapper;

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		Map<String, Object> attributes = oAuth2User.getAttributes();

		Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
		Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

		String email = (String) kakaoAccount.get("email");
		String nickname = (String) profile.get("nickname");
		String providerId = String.valueOf(attributes.get("id"));

		Users existingUser = userMapper.findUserByEmail(email);

		if (existingUser == null) {
			Users newUser = new Users();
			newUser.setEmail(email);
			newUser.setNickname(nickname);
			newUser.setPassword(UUID.randomUUID().toString());
			newUser.setProvider("KAKAO");
			newUser.setProviderId(providerId);

			userMapper.insertSocialUser(newUser);

			UserAuth userAuth = new UserAuth();
			long userId = userMapper.getUserId(email);
			userAuth.setUserId(userId);
			userAuth.setAuthority("ROLE_USER");
			userMapper.addAuth(userAuth);

			existingUser = userMapper.findUserByEmail(email);
		}

		return new CustomUser(existingUser);
	}
}
