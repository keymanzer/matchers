package kr.or.kosa.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kosa.mapper.UserMapper;
import kr.or.kosa.user.dto.UserAuth;
import kr.or.kosa.user.dto.Users;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final PasswordEncoder passwordEncoder;

	private final UserMapper userMapper;

	private final NicknameGeneratorService nicknameGeneratorService;

	@Override
	@Transactional
	public int signUp(Users user) {
		String userPw = user.getPassword();
		String encodedUserPw = passwordEncoder.encode(userPw);
		user.setPassword(encodedUserPw);

		String userNickname = nicknameGeneratorService.generateNickname();
		user.setNickname(userNickname);

		int result = userMapper.signUp(user);

		if (result > 0) {
			UserAuth userAuth = new UserAuth();
			long userId = userMapper.getUserId(user.getEmail());
			userAuth.setUserId(userId);
			userAuth.setAuthority("ROLE_USER");
			result += userMapper.addAuth(userAuth);
		}

		return result;
	}

	@Override
	public int emailExists(String email) {
		return userMapper.emailExists(email);
	}

}
