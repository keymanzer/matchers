package kr.or.kosa.common;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

	private final S3Client s3Client;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	public String uploadFile(MultipartFile file, String folder) throws IOException {
		try {
			String key = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

			PutObjectRequest putRequest = PutObjectRequest.builder().bucket(bucket).key(key)
					.contentType(file.getContentType()).build();

			s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

			return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + key;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("S3 업로드 실패", e);
		}
	}

	public void deleteFile(String key) {
		DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder().bucket(bucket).key(key).build();
		s3Client.deleteObject(deleteRequest);
	}

	public String getFileUrl(String key) {
		return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + key;
	}
}
