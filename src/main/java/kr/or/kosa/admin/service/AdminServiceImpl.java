package kr.or.kosa.admin.service;

import java.util.List;

import kr.or.kosa.chat.controller.SseController;
import kr.or.kosa.mapper.UserMapper;
import kr.or.kosa.user.dto.Users;
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
	private final UserMapper userMapper;
	private final SseController sseController;

	@Override
	public List<ExpertDetail> getExpertList() {
		return adminMapper.getExpertList();
	}

	@Override
	@Transactional
	public void approveExpertByEmail(String email, Long senderId) {
		adminMapper.approveExpertByEmail(email);
		adminMapper.insertExpertAuth(email);

		Users receivedUser = userMapper.findUserByEmail(email);
		String message = receivedUser.getNickname() + "님, 전문가 인증이 완료되었습니다.";
		sseController.sendNotification(receivedUser.getUserId(),senderId, message);

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
