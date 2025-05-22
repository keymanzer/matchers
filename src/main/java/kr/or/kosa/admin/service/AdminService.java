package kr.or.kosa.admin.service;

import java.util.List;

import kr.or.kosa.admin.dto.ExpertCategory;
import kr.or.kosa.admin.dto.ExpertDetail;
import kr.or.kosa.admin.dto.ExpertLicense;
import kr.or.kosa.admin.dto.ExpertLocation;

public interface AdminService {

	List<ExpertDetail> getExpertList();

	ExpertDetail getExpertByUserId(Long userId);

	List<ExpertLicense> getLicenses(Long userId);

	List<ExpertCategory> getCategories(Long userId);

	List<ExpertLocation> getLocations(Long userId);

	void approveExpertByEmail(String email, Long senderId);
}
