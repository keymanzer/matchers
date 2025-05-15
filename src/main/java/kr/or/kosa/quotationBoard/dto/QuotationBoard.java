package kr.or.kosa.quotationBoard.dto;
import lombok.Data;

import java.util.List;

@Data
public class QuotationBoard {
    private int postId;
    private String title;        // 게시글 제목
    private String content;
    private String userNickname; // 작성자 닉네임
    private int userId;         // 유저 ID
    private int expertId;
    private String state;
    private int categoryId;
    private List<location> quotationLocations;
}
