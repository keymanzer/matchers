package kr.or.kosa.mapper;

import kr.or.kosa.user.dto.UserAuth;
import kr.or.kosa.user.dto.Users;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
	long getUserId(String email);

	int emailExists(String email);

	Users login(String username);

	int signUp(Users user);

	void insertSocialUser(Users user);

	int addAuth(UserAuth userAuth);

}
