package kr.or.kosa.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kosa.admin.dto.ExpertCategory;
import kr.or.kosa.admin.dto.ExpertDetail;
import kr.or.kosa.admin.dto.ExpertLicense;
import kr.or.kosa.admin.dto.ExpertLocation;
import kr.or.kosa.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

	private final AdminMapper adminMapper;

	@Override
	public List<ExpertDetail> getExpertList() {
		return adminMapper.getExpertList();
	}

	@Override
	@Transactional
	public void approveExpertByEmail(String email) {
		adminMapper.approveExpertByEmail(email);
		adminMapper.insertExpertAuth(email);
	}

	@Override
	public List<ExpertLicense> getLicenses(Long userId) {
		return adminMapper.getLicenses(userId);
	}

	@Override
	public List<ExpertCategory> getCategories(Long userId) {
		return adminMapper.getCategories(userId);
	}

	@Override
	public List<ExpertLocation> getLocations(Long userId) {
		return adminMapper.getLocations(userId);
	}

	@Override
	public ExpertDetail getExpertByUserId(Long userId) {
		return adminMapper.getExpertByUserId(userId);
	}

}
