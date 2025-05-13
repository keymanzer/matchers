package kr.or.kosa.community.dto;

import java.time.LocalDateTime;

import kr.or.kosa.user.dto.Users;
import lombok.Data;

@Data
public class CommunityBoard {
	
	//private Board board;
	private Long postId;
	private LocalDateTime lastUpdatedAt;
	private Long views;
	private Users users;

}
