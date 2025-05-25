package kr.or.kosa.findedExpert.mapper;

import kr.or.kosa.findedExpert.dto.CategoryDto;
import kr.or.kosa.findedExpert.dto.ExpertCardDto;
import kr.or.kosa.findedExpert.dto.LocationDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FindedExpertMapper {
    
    List<CategoryDto> getAllCategories();
    
    List<LocationDto> getAllLocations();
    
    List<ExpertCardDto> getTopExperts(@Param("limit") int limit);
    
    List<ExpertCardDto> getRecommendedExpertsByUserCategory(@Param("userId") Long userId);
    
    List<ExpertCardDto> filterExperts(@Param("categoryId") Long categoryId, @Param("locationId") Long locationId);
    
    Long getTotalExpertCount();
    
    Long getActiveExpertCount();
    
    // 전문가 경력 정보 조회 메서드 수정
    String getExpertCareer(@Param("userId") Long userId);
}
