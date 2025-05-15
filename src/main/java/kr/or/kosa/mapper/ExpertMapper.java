package kr.or.kosa.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import kr.or.kosa.expert.dto.Category;
import kr.or.kosa.expert.dto.Location;

@Mapper
public interface ExpertMapper {
	int userInfoUpdate(Map<String, Object> params);

	int expertInfoReg(Map<String, Object> params);

	int expertLocReg(Map<String, Object> params);

	int expertCtgReg(Map<String, Object> params);

	int expertLcsReg(Map<String, Object> params);

	List<Location> getLocationList();

	List<Category> getCategoryList();
}
