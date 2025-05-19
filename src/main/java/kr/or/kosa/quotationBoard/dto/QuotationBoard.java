package kr.or.kosa.quotationBoard.dto;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    private LocalDateTime createdAt; // 추가
    private List<location> quotationLocations;
    // --- 폼 바인딩용 필드들 ---
    /** create 폼의 체크박스(name="locationIds") 바인딩 */
    private List<Integer> locationIds;
    /** multipart/form-data 의 file inputs(name="attachedFiles") 바인딩 */
    private List<MultipartFile> attachedFiles;
}
