package kr.or.kosa.attachedFile.mapper;

import kr.or.kosa.attachedFile.dto.AttachedFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttachedFileMapper {

    // 첨부파일을 DB에 삽입
    void insertAttachedFile(AttachedFile attachedFile);
}
