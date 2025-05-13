package kr.or.kosa.community.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	
}
