package kr.or.kosa.qa.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class QaBoardComment {

	private Long commentId;
	private Long postId;
	private String content;
    private LocalDateTime createdAt;
    private String userNickname;
	
}
