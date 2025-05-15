package kr.or.kosa.community.dto;

import java.time.LocalDateTime;

import kr.or.kosa.user.dto.Users;
import lombok.Data;

@Data
public class CommunityBoard {

	private Long postId;
	private LocalDateTime lastUpdatedAt;
	private Long views;
	
	//private Board board;
	private Long boardSeq;
	private String title;
	private String content;
	private LocalDateTime createdAt;
	private String nickname;
	private Long userId;
	
	private Users users;

}
