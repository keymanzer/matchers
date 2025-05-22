package kr.or.kosa.mapper;

import kr.or.kosa.user.dto.UserAuth;
import kr.or.kosa.user.dto.Users;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
	long getUserId(String email);

	int emailExists(String email);

	Users findUserByEmail(String username);

	int signUp(Users user);

	void insertSocialUser(Users user);

	int addAuth(UserAuth userAuth);

	void updateUserProfileImg(Map<String, Object> params);

	void updateUserNickname(Map<String, Object> params);

	String verifyPassword(Long userId);

	void updatePassword(Map<String, Object> params);

	void withdrawUser(Long userId);

	// 채팅 쪽에서 사용하기 위해서 2개 메서드 추가 -- 백승호
	String findEmailByUserId(Long userId);
	String findNameById(Long userId);
}
