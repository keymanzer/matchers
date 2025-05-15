package kr.or.kosa.attachedFile.service;

import kr.or.kosa.attachedFile.dto.AttachedFile;
import kr.or.kosa.attachedFile.mapper.AttachedFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttachedFileService {

    @Autowired
    private AttachedFileMapper attachedFileMapper;  // Mapper를 주입받습니다.

    // 첨부파일을 DB에 저장
    public void saveAttachedFile(AttachedFile attachedFile) {
        attachedFileMapper.insertAttachedFile(attachedFile);  // Mapper에서 insert 쿼리 호출
    }
}
