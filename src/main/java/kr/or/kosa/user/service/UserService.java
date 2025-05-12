package kr.or.kosa.user.service;

import kr.or.kosa.user.dto.Users;

public interface UserService {
	
	int emailExists(String email);

	int signUp(Users user);
}
