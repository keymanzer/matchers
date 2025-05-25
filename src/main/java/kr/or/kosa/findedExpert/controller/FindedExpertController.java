package kr.or.kosa.findedExpert.controller;

import kr.or.kosa.findedExpert.dto.ExpertCardDto;
import kr.or.kosa.findedExpert.service.FindedExpertService;
import kr.or.kosa.user.dto.CustomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/findedExpert")
@RequiredArgsConstructor
public class FindedExpertController {

    private final FindedExpertService findedExpertService;

    @GetMapping("")
    public String findExperts(Model model, Authentication authentication) {
        model.addAttribute("categories", findedExpertService.getAllCategories());
        model.addAttribute("locations", findedExpertService.getAllLocations());

        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUser) {
            userId = ((CustomUser) authentication.getPrincipal()).getUserId();
        }

        // 추천 전문가 목록 가져오기 (로그인한 경우 사용자 관심 카테고리 기반, 아닌 경우 인기순)
        List<ExpertCardDto> recommendedExperts;
        if (userId != null) {
            recommendedExperts = findedExpertService.getRecommendedExpertsByUserCategory(userId);
            if (recommendedExperts.isEmpty()) {
                // 사용자 카테고리 기반 추천이 없으면 인기순으로 대체
                recommendedExperts = findedExpertService.getTopExperts(8);
            }
        } else {
            recommendedExperts = findedExpertService.getTopExperts(8);
        }
        
        model.addAttribute("experts", recommendedExperts);
        return "findedExpert/expertList";
    }

    @GetMapping("/filter")
    @ResponseBody
    public List<ExpertCardDto> filterExperts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId) {

        
        try {    
            List<ExpertCardDto> result = findedExpertService.filterExperts(categoryId, locationId);
            log.info("필터링 결과 - 전문가 수: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("필터링 처리 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    @GetMapping("/counts")
    @ResponseBody
    public Map<String, Long> getExpertCounts() {
        return findedExpertService.getExpertCounts();
    }
}
