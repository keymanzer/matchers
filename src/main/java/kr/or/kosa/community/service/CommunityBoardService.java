package kr.or.kosa.community.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kosa.community.dto.CommunityBoard;
import kr.or.kosa.community.mapper.CommunityBoardMapper;

@Service
public class CommunityBoardService {
	
	@Autowired
	private CommunityBoardMapper communityBoardMapper;

	public void setCommunityBoardMapper(CommunityBoardMapper communityBoardMapper) {
		this.communityBoardMapper = communityBoardMapper;
	}
	
	public List<CommunityBoard> getPostList(){
		return communityBoardMapper.getPostList();
	}
	
	public CommunityBoard getPostbyId(Long postId){
		return communityBoardMapper.getPostbyId(postId);
	}
	
	public void insertPost(CommunityBoard communityBoard) {
		communityBoardMapper.insertPost(communityBoard);
	}
	
	public void updatePost(CommunityBoard communityBoard) {
		communityBoardMapper.updatePost(communityBoard);
		//BoardMapper.updatePost(communityBoard);
	}

	public void deletePost(Long postId) {
		communityBoardMapper.deletePost(postId);	
		//BoardMapper.deletePost(postId);
	}
	
}
