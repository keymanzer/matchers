package kr.or.kosa.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.kosa.community.dto.CommunityBoard;
import kr.or.kosa.user.dto.Users;

@Mapper
public interface CommunityBoardMapper {
	
	List<CommunityBoard> getPostList();
	
	List<CommunityBoard> getPostListByViews();

	CommunityBoard getPostbyId(@Param("postId") Long postId);
	
	void insertPost(CommunityBoard communityBoard);
	
	void updatePost(CommunityBoard communityBoard);
	
	void increaseViewCount(@Param("postId") Long postId);
	
	void deletePost(@Param("postId") Long postId);

}
