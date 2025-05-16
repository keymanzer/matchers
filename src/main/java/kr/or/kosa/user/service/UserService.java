package kr.or.kosa.user.service;

import org.springframework.web.multipart.MultipartFile;

import kr.or.kosa.user.dto.Users;

public interface UserService {

	Users findUserById(String email);

	int emailExists(String email);

	int signUp(Users user);

	String saveProfileImage(MultipartFile file, Long userId);

	void updateNickname(Long userId, String nickname);

	boolean verifyPassword(Long userId, String password);

	void updatePassword(Long userId, String password);

	void withdrawUser(Long userId);
}
