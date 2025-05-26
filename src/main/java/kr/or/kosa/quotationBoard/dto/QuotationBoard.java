package kr.or.kosa.quotationBoard.dto;
import kr.or.kosa.attachedFile.dto.AttachedFile;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class QuotationBoard {
    private long postId;
    private String title;        // 게시글 제목
    private String content;
    private String userNickname; // 작성자 닉네임
    private long userId;         // 유저 ID
    private long expertId;
    private String state;
    private int categoryId;
    private LocalDateTime createdAt; // 추가
    private List<location> quotationLocations;
    private String categoryName;
    // --- 폼 바인딩용 필드들 ---
    /** create 폼의 체크박스(name="locationIds") 바인딩 */
    private List<Integer> locationIds;
    /** multipart/form-data 의 file inputs(name="attachedFiles") 바인딩 */
    // --- form 바인딩용: 업로드 받을 때만 쓰는 필드 ---
    @JsonIgnore
    private List<MultipartFile> uploadFiles;

    // --- API 응답용: DB에서 조회한 첨부파일 리스트 ---
    private List<AttachedFile> attachedFiles;
    public String getLocationIdsAsString() {
        if (quotationLocations == null || quotationLocations.isEmpty()) {
            return "";
        }

        return quotationLocations.stream()
                .map(location -> String.valueOf(location.getLocationId()))
                .collect(Collectors.joining(","));
    }

}
