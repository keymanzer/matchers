package kr.or.kosa.community.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kosa.attachedFile.mapper.AttachedFileMapper;
import kr.or.kosa.board.mapper.BoardMapper;
import kr.or.kosa.community.dto.CommunityBoard;
import kr.or.kosa.community.mapper.CommunityBoardMapper;


@Service
public class CommunityBoardService {
	
	@Autowired
	private CommunityBoardMapper communityBoardMapper;
	
	@Autowired
	private BoardMapper boardMapper;
	
	@Autowired
	private AttachedFileMapper attachedFileMapper;
	
	public void setCommunityBoardMapper(CommunityBoardMapper communityBoardMapper) {
		this.communityBoardMapper = communityBoardMapper;
	}
	
	public List<CommunityBoard> getPostList(){
		return communityBoardMapper.getPostList();
	}
	
	public List<CommunityBoard> getPostListByViews(){
		return communityBoardMapper.getPostListByViews();
	}
	
	public CommunityBoard getPostbyId(Long postId){
		return communityBoardMapper.getPostbyId(postId);
	}
	
	public void insertPost(CommunityBoard communityBoard) {
		communityBoardMapper.insertPost(communityBoard);
	}
	
	@Transactional
	public void updatePost(CommunityBoard communityBoard) {
		communityBoardMapper.updatePost(communityBoard);
		boardMapper.updateBoard(communityBoard.getPostId(), communityBoard.getTitle(), communityBoard.getContent());
	}
	
	public void increaseViewCount(Long postId) {
	    communityBoardMapper.increaseViewCount(postId);
	}
	
		
    @Transactional
    public void deletePost(Long postId) {
    	attachedFileMapper.deleteAttachedFilesByPostId(postId);
    	communityBoardMapper.deletePost(postId);
        boardMapper.deleteBoard(postId);
    }

}
