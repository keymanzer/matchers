package kr.or.kosa.admin.dto;

import java.sql.Date;

import lombok.Data;

@Data
public class ExpertDetail {

	private Long userId;
	private String email;
	private String nickname;
	private String profileImg;
	private String career;
	private String status;
	private Date reviewedAt;
}
