package kr.or.kosa.findedExpert.service;

import kr.or.kosa.findedExpert.dto.CategoryDto;
import kr.or.kosa.findedExpert.dto.ExpertCardDto;
import kr.or.kosa.findedExpert.dto.LocationDto;
import kr.or.kosa.findedExpert.mapper.FindedExpertMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindedExpertService {

    private final FindedExpertMapper findedExpertMapper;


    public List<CategoryDto> getAllCategories() {
        return findedExpertMapper.getAllCategories();
    }


    public List<LocationDto> getAllLocations() {
        return findedExpertMapper.getAllLocations();
    }

    /**
     * 인기 전문가 목록 (완료된 견적 게시판 수 기준)
     */
    public List<ExpertCardDto> getTopExperts(int limit) {
        return findedExpertMapper.getTopExperts(limit);
    }

    /**
     * 사용자가 작성한 견적 게시판의 카테고리와 일치하는 전문가 목록
     */
    public List<ExpertCardDto> getRecommendedExpertsByUserCategory(Long userId) {
        return findedExpertMapper.getRecommendedExpertsByUserCategory(userId);
    }

    /**
     * 카테고리와 지역으로 전문가를 필터링
     */
    public List<ExpertCardDto> filterExperts(Long categoryId, Long locationId) {
        try {
            log.info("서비스: 필터링 실행 - 카테고리 ID: {}, 지역 ID: {}", categoryId, locationId);
            List<ExpertCardDto> experts = findedExpertMapper.filterExperts(categoryId, locationId);
            
            // null 체크
            if (experts == null) {
                log.warn("매퍼가 null을 반환했습니다.");
                return new ArrayList<>();
            }
            
            log.info("서비스: 필터링 결과 - {} 명의 전문가 찾음", experts.size());
            return experts;
        } catch (Exception e) {
            log.error("전문가 필터링 중 오류 발생", e);
            return new ArrayList<>(); // 오류 시 빈 목록 반환
        }
    }

    public Map<String, Long> getExpertCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("total", findedExpertMapper.getTotalExpertCount());
        counts.put("active", findedExpertMapper.getActiveExpertCount());
        return counts;
    }
}
