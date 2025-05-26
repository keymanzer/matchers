package kr.or.kosa.community.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.kosa.community.dto.CommunityBoard;
import kr.or.kosa.community.service.CommunityBoardService;

@RestController
@RequestMapping("/user/community/api")
public class CommunityBoardApiController {

    @Autowired
    private CommunityBoardService communityBoardService;

    @GetMapping("/top5")
    public List<CommunityBoard> getTop5Posts() {
        List<CommunityBoard> allPosts = communityBoardService.getPostListByViews();
        return allPosts.size() > 5 ? allPosts.subList(0, 5) : allPosts;
    }
}