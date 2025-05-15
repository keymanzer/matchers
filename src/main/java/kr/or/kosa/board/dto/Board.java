package kr.or.kosa.board.dto;


import kr.or.kosa.attachedFile.dto.AttachedFile;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Board {

        private int postId;         // 게시글 ID
        private String title;        // 게시글 제목
        private String content;      // 게시글 내용
        private LocalDateTime createdAt; // 생성일시
        private String userNickname; // 작성자 닉네임
        private int userId;         // 유저 ID
        private List<AttachedFile> attachedFiles; // 첨부파일 목록

}
