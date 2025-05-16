package kr.or.kosa.community.dto;

import java.time.LocalDateTime;

import kr.or.kosa.board.dto.Board;
import kr.or.kosa.user.dto.Users;
import lombok.Data;

@Data
public class CommunityBoard {

	private Long postId;
	private LocalDateTime lastUpdatedAt;
	private Long views;
    private Long userId;
    private String userNickname;
	private String title;
	private String content;
	private Long boardSeq;
	private Board board;
	private Users users;

}
