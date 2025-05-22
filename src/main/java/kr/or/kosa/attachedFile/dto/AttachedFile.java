package kr.or.kosa.attachedFile.dto;

import lombok.Data;

@Data
public class AttachedFile {
    private Long attachedFileId; // 첨부파일 ID
    private String name;         // 첨부파일 이름
    private String path;         // 첨부파일 경로
    private long postId;         // 게시글 ID (foreign key)
}
