package kr.or.kosa.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kr.or.kosa.mapper.UserMapper;
import kr.or.kosa.user.dto.Users;
import kr.or.kosa.user.dto.CustomUser;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomDetailService implements UserDetailsService {

	private final UserMapper userMapper;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Users user = userMapper.login(username);

		if (user == null) {
			throw new UsernameNotFoundException(username + "으로 가입된 정보가 없습니다 ");
		}

		CustomUser customerUser = new CustomUser(user);

		return customerUser;
	}

}
