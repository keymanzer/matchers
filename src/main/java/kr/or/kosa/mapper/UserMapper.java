package kr.or.kosa.mapper;

import kr.or.kosa.user.dto.Users;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

	Users login(String username);

}
