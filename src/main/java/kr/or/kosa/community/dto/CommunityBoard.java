package kr.or.kosa.community.dto;

import java.time.LocalDateTime;
import java.util.List;

import kr.or.kosa.attachedFile.dto.AttachedFile;
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
	private Board board;
	private Users users;
	private List<AttachedFile> attachedFiles; // 첨부파일 목록
	private Integer attachedFileExists; // 0 또는 1
	
}
