package kr.or.kosa.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.or.kosa.mapper.UserMapper;
import kr.or.kosa.user.dto.Users;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final PasswordEncoder passwordEncoder;

	private final UserMapper userMapper;

	@Override
	public Users login(String username) {
		Users user = userMapper.login(username);
		return user;
	}

}
