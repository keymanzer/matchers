package kr.or.kosa.community.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class CommunityBoardComment {

	private Long commentId;
	private Long postId;
	private Long parentCommentId;
	private String content;
    private LocalDateTime createdAt;
    private String userNickname;
    private List<CommunityBoardComment> replies;

}
