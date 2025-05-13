package kr.or.kosa.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.or.kosa.community.dto.CommunityBoard;
import lombok.extern.slf4j.Slf4j;

@Mapper
public interface CommunityBoardMapper {
	
	List<CommunityBoard> getPostList();

	//CommunityBoard getPostListbyId(Long postId);
	
	//void insertPost(CommunityBoard communityBoard);
	
	//void updatePost(CommunityBoard communityBoard);
	
	//void deletePost(Long postId);
	
}
