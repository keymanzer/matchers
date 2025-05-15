package kr.or.kosa.expert.dto;

import java.sql.Date;

import lombok.Data;

@Data
public class Expert {

	private Long userId;
	private String career;
	private String profileImg;
	private String status;
	private Date updatedAt;
	private Date reviewedAt;
}
