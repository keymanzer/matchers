package kr.or.kosa.qa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kosa.attachedFile.dto.AttachedFile;
import kr.or.kosa.attachedFile.mapper.AttachedFileMapper;
import kr.or.kosa.board.mapper.BoardMapper;
import kr.or.kosa.qa.dto.QaBoard;
import kr.or.kosa.qa.mapper.QaBoardMapper;

@Service
public class QaBoardService {

    @Autowired
    private QaBoardMapper qaBoardMapper;
    
    @Autowired
    private BoardMapper boardMapper;
    
	@Autowired
	private AttachedFileMapper attachedFileMapper;

    public void setQaBoardMapper(QaBoardMapper qaBoardMapper) {
		this.qaBoardMapper = qaBoardMapper;
	}

	public void setBoardMapper(BoardMapper boardMapper) {
		this.boardMapper = boardMapper;
	}

	public void setAttachedFileMapper(AttachedFileMapper attachedFileMapper) {
		this.attachedFileMapper = attachedFileMapper;
	}

	public List<QaBoard> getPostList() {
        return qaBoardMapper.getPostList();
    }
	
	public List<QaBoard> getPostListByViews() {
        return qaBoardMapper.getPostListByViews();
    }

    public QaBoard getPostbyId(Long postId) {
        QaBoard post = qaBoardMapper.getPostbyId(postId);

        List<AttachedFile> files = attachedFileMapper.findAttachedFilesByPostId(postId.intValue());
        post.setAttachedFiles(files);

        return post;
    }

    public void insertPost(QaBoard qaBoard) {
        qaBoardMapper.insertPost(qaBoard);
    }
    
    @Transactional
    public void updatePost(QaBoard qaBoard) {
        qaBoardMapper.updatePost(qaBoard);
        boardMapper.updateBoard(qaBoard.getPostId().intValue(), qaBoard.getTitle(), qaBoard.getContent());
    }
    
	public void increaseViewCount(Long postId) {
		qaBoardMapper.increaseViewCount(postId);
	}
    
    @Transactional
    public void deletePost(Long postId) {
    	attachedFileMapper.deleteAttachedFilesByPostId(postId.intValue());
        qaBoardMapper.deletePost(postId);
        boardMapper.deleteBoard(postId.intValue());
    }
}
