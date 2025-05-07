package kr.or.kosa.user.dto;

import lombok.Data;

@Data
public class UserAuth {

	private Long userId;
	private String authority;
}
