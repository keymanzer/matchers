package kr.or.kosa.user.dto;

import java.sql.Date;
import java.util.List;

import lombok.Data;

@Data
public class Users {

	private Long userId;
	private String email;
	private String password;
	private String nickname;
	private String profileImg;
	private String provider;
	private String providerId;
	private Character enabled;
	private Date createdAt;
	private Date updatedAt;
	private Character isDeleted;

	List<UserAuth> authList;
}
