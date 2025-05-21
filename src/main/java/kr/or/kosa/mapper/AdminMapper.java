package kr.or.kosa.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.or.kosa.admin.dto.ExpertCategory;
import kr.or.kosa.admin.dto.ExpertDetail;
import kr.or.kosa.admin.dto.ExpertLicense;
import kr.or.kosa.admin.dto.ExpertLocation;

@Mapper
public interface AdminMapper {

	List<ExpertDetail> getExpertList();

	ExpertDetail getExpertByUserId(Long userId);

	List<ExpertLicense> getLicenses(Long userId);

	List<ExpertCategory> getCategories(Long userId);

	List<ExpertLocation> getLocations(Long userId);

	void approveExpertByEmail(String email);

	void insertExpertAuth(String email);
}
