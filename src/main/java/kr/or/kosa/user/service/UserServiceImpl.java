package kr.or.kosa.user.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kr.or.kosa.common.S3Service;
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

	private final S3Service s3Service;

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

	@Override
	public String saveProfileImage(MultipartFile file, Long userId) {
		String profileImg = null;
		try {
			profileImg = s3Service.uploadFile(file, "profile");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, Object> userImgParams = new HashMap<>();
		userImgParams.put("userId", userId);
		userImgParams.put("profileImg", profileImg);
		userMapper.updateUserProfileImg(userImgParams);
		return profileImg;
	}

	@Override
	public Users findUserById(String email) {
		return userMapper.findUserByEmail(email);
	}

	@Override
	public void updateNickname(Long userId, String nickname) {
		Map<String, Object> userNicknameParams = new HashMap<>();
		userNicknameParams.put("userId", userId);
		userNicknameParams.put("nickname", nickname);
		userMapper.updateUserNickname(userNicknameParams);
	}

	@Override
	public void updatePassword(Long userId, String password) {
		Map<String, Object> userPasswordParams = new HashMap<>();
		String encodedPassword = passwordEncoder.encode(password);
		userPasswordParams.put("userId", userId);
		userPasswordParams.put("password", encodedPassword);
		userMapper.updatePassword(userPasswordParams);
	}

	@Override
	public boolean verifyPassword(Long userId, String password) {
		String encodedPassword = userMapper.verifyPassword(userId);
		boolean match = passwordEncoder.matches(password, encodedPassword);
		return match;
	}

	@Override
	public void withdrawUser(Long userId) {
		userMapper.withdrawUser(userId);
	}
}
