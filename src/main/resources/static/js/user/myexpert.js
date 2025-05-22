const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

// 공통 fetch POST 함수 (json 요청/응답)
async function postJson(url, data) {
	const res = await fetch(url, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
			[csrfHeader]: csrfToken,
		},
		body: JSON.stringify(data),
	});

	if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);

	return res.json();
}

// 현재 비밀번호 검증
async function verifyCurrentPassword(password) {
	try {
		const data = await postJson('/user/mypage/verify-password', { password });
		return data.success === true;
	} catch {
		return false;
	}
}

// 비밀번호 변경
document.getElementById('changePasswordLink').addEventListener('click', async (e) => {
	e.preventDefault();

	const { value: currentPassword } = await Swal.fire({
		title: '현재 비밀번호 입력',
		input: 'password',
		inputLabel: '현재 비밀번호를 입력하세요',
		inputPlaceholder: '현재 비밀번호',
		confirmButtonText: '입력',
		cancelButtonText: '취소',
		inputAttributes: { autocapitalize: 'off', autocomplete: 'current-password' },
		showCancelButton: true,
	});

	if (!currentPassword) return;

	const isValid = await verifyCurrentPassword(currentPassword);
	if (!isValid) {
		return Swal.fire('오류', '현재 비밀번호가 올바르지 않습니다.', 'error');
	}

	const { value: newPassword } = await Swal.fire({
		title: '새 비밀번호 입력',
		input: 'password',
		inputLabel: '새 비밀번호를 입력하세요',
		inputPlaceholder: '8자 이상, 대문자 1개, 특수기호 포함',
		confirmButtonText: '입력',
		cancelButtonText: '취소',
		inputAttributes: { autocapitalize: 'off', autocomplete: 'new-password' },
		inputValidator: (value) => {
			const pwPattern = /^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/;
			if (!value) return '비밀번호를 입력해주세요';
			if (!pwPattern.test(value)) return '8자 이상, 대문자 1개, 특수기호 포함해야 합니다.';
		},
		showCancelButton: true,
	});

	if (!newPassword) return;

	try {
		const data = await postJson('/user/mypage/change-password', { password: newPassword });
		if (data.success) {
			Swal.fire('완료', '비밀번호가 변경되었습니다.', 'success');
		} else {
			Swal.fire('오류', data.message || '비밀번호 변경에 실패했습니다.', 'error');
		}
	} catch {
		Swal.fire('오류', '서버와 통신 중 오류가 발생했습니다.', 'error');
	}
});

// 회원 탈퇴
document.getElementById('withdrawLink').addEventListener('click', async (e) => {
	e.preventDefault();

	const { value: password } = await Swal.fire({
		title: '회원 탈퇴',
		input: 'password',
		inputLabel: '비밀번호를 입력하세요',
		inputPlaceholder: '비밀번호',
		inputAttributes: {
			autocapitalize: 'off',
			autocomplete: 'current-password',
		},
		showCancelButton: true,
		confirmButtonText: '탈퇴',
		cancelButtonText: '취소',
		showLoaderOnConfirm: true,
		preConfirm: async (pw) => {
			if (!pw) {
				Swal.showValidationMessage('비밀번호를 입력해주세요');
				return false;
			}
			try {
				const data = await postJson('/user/mypage/verify-password', { password: pw });
				if (!data.success) {
					Swal.showValidationMessage('비밀번호가 올바르지 않습니다.');
					return false;
				}
				return pw;
			} catch {
				Swal.showValidationMessage('서버와 통신 중 오류가 발생했습니다.');
				return false;
			}
		},
		allowOutsideClick: () => !Swal.isLoading(),
	});

	if (!password) return;

	try {
		const data = await postJson('/user/mypage/withdraw', { password });
		if (data.success) {
			await Swal.fire('완료', '회원 탈퇴가 완료되었습니다.', 'success');
			// 로그아웃 처리
			await fetch('/logout', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/x-www-form-urlencoded',
					[csrfHeader]: csrfToken,
				},
			});
			window.location.href = '/';
		} else {
			Swal.fire('오류', data.message || '회원 탈퇴에 실패했습니다.', 'error');
		}
	} catch {
		Swal.fire('오류', '서버와 통신 중 오류가 발생했습니다.', 'error');
	}
});