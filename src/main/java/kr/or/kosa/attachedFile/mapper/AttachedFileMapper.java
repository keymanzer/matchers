package kr.or.kosa.attachedFile.mapper;

import kr.or.kosa.attachedFile.dto.AttachedFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// kr.or.kosa.attachedFile.mapper.AttachedFileMapper.java
@Mapper
public interface AttachedFileMapper {
    /** 게시글에 연결된 첨부파일 메타 정보 저장 */
    void insertAttachedFile(AttachedFile attachedFile);

    /** 게시글 전체 첨부파일 메타 삭제 */
    void deleteAttachedFilesByPostId(@Param("postId") long postId);

    /** 게시글별 첨부파일 목록 조회 */
    List<AttachedFile> findAttachedFilesByPostId(@Param("postId") long postId);
}
