package kr.or.kosa.attachedFile.service;

import kr.or.kosa.attachedFile.dto.AttachedFile;
import kr.or.kosa.attachedFile.mapper.AttachedFileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachedFileService {

    private final AttachedFileMapper attachedFileMapper;

    @Value("${spring.servlet.multipart.location}")
    private String uploadDirectory;

    /** 첨부파일 저장(DB + 물리경로) */
    public void saveAttachedFileMetadata(AttachedFile af) {
        attachedFileMapper.insertAttachedFile(af);
    }
    /** postId 기준으로 파일 메타 조회 */
    public List<AttachedFile> findByPostId(long postId) {
        return attachedFileMapper.findAttachedFilesByPostId(postId);
    }

    /** postId 기준으로 물리+DB 모두 삭제 */
    @Transactional
    public void deleteAttachedFilesByPostId(long postId) {
        // 1) DB에서 메타만 조회
        List<AttachedFile> files = attachedFileMapper.findAttachedFilesByPostId(postId);
        System.out.println("files = " + files);
        // 2) 물리 삭제
        for (AttachedFile f : files) {
            File diskFile = new File(uploadDirectory, f.getName());
            if (diskFile.exists()) {
                diskFile.delete();
            }
        }
        // 3) DB 메타 삭제
        attachedFileMapper.deleteAttachedFilesByPostId(postId);
    }
}
