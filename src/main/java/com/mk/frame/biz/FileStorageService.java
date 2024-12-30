package com.mk.frame.biz;

import com.mk.frame.biz.model.dto.FiledownloadInfo;
import com.mk.frame.biz.model.dto.FileuploadInfo;
import com.mk.frame.comm.utils.FileUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class FileStorageService {
    @Value("${env}")
	private String environment;

	@Value("${storage.file.location}")
	private String uploadRootPath;

	@PostConstruct
	public void createDir() {
		new File(uploadRootPath).mkdirs();
	}

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

	/**
	 * 파일 업로드 (단건)
	 */
	public FileuploadInfo store(MultipartFile file, String etcInfo) throws IOException {
		String path = UUID.randomUUID().toString();
		try {
			if (file.isEmpty()) {
				throw new RuntimeException("File is empty.");
			}
			String subPath = formatter.format(LocalDateTime.now());
			File dir = new File(uploadRootPath, subPath);
			dir.mkdirs();
			File destinationFile = new File(dir, path);
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			String originalFilename = file.getOriginalFilename();
			int lastIdx = originalFilename.lastIndexOf(".");
			String fileExt = lastIdx > -1 ? originalFilename.substring(lastIdx + 1).toLowerCase() : "";

			return FileuploadInfo.builder()
					.path(subPath + File.separator + path)
					.etcInfo(etcInfo)
					.fileExtension(fileExt)
					.filename(originalFilename)
					.fileSize(file.getSize())
					.build();

		} catch (Exception e) {
			deleteUploadedFile(path);
			throw e;
		}
	}

	/**
	 * 업로드된 파일 삭제
	 */
	public void deleteUploadedFile(String path) {
		try {
			String targetPath = FileUtils.getFilteredPath(path);
			File f = new File(uploadRootPath, targetPath);
			if (f.isFile()) {
				f.delete();
			}
		} catch (Exception e) {
			log.warn("=== [ignore] deleting file : {}", path, e);
		}
	}

	public FileuploadInfo storeToTempDir(MultipartFile file, String etcInfo) throws IOException {
		String path = UUID.randomUUID().toString();
		try {
			if (file.isEmpty()) {
				throw new RuntimeException("File is empty.");
			}
			String subPath = "temp";
			// (예) /image/temp
			File dir = new File(uploadRootPath, subPath);
			dir.mkdirs();
			File destinationFile = new File(dir, path);
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			String originalFilename = file.getOriginalFilename();
			int lastIdx = originalFilename.lastIndexOf(".");
			String fileExt = lastIdx > -1 ? originalFilename.substring(lastIdx + 1).toLowerCase() : "";

			return FileuploadInfo.builder()
					.path(subPath + File.separator + path)
					.etcInfo(etcInfo)
					.fileExtension(fileExt)
					.filename(originalFilename)
					.fileSize(file.getSize())
					.build();

		} catch (Exception e) {
			deleteUploadedFile(path);
			throw e;
		}
	}

	/**
	 * 임시저장경로에 있는 파일을 실제 사용할 경로로 이동한다.
	 *
	 * @param tempPath 임시저장경로
	 */
	public String moveToActualDir(String tempPath) throws IOException {
		try {
			String subPath = formatter.format(LocalDateTime.now());
			File dir = new File(uploadRootPath, subPath);
			dir.mkdirs();

			String sourceSubPath = FileUtils.getFilteredPath(tempPath);
			Path sourcePath = Paths.get(uploadRootPath, sourceSubPath);
			Path targetPath = Paths.get(uploadRootPath, subPath, sourcePath.getFileName().toString());

			// temp 파일이 존재하는데, target파일이 존재하지 않으면 지극히 정상적인 상태임. => move
			if (Files.exists(sourcePath) && !Files.exists(targetPath)) {
				Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
				return subPath + File.separator + targetPath.getFileName().toString();
			}

			// 만일 temp파일이 존재하지 않는데, target 파일이 존재하면, 이미 옮겨진 거라고 간주하고서(정상) 통과
			if (!Files.exists(sourcePath) && Files.exists(targetPath)) {
				log.warn("=== 파일이 이미 옮겨짐 ({})", tempPath);
				return subPath + File.separator + targetPath.getFileName().toString();
			}

			// temp 파일이 존재하는데, target파일도 존재하면 => WTF! 복사하자.
			if (Files.exists(sourcePath) && Files.exists(targetPath)) {
				log.warn("=== temp파일과 target파일이 모두 존재, 덮어쓸것임 ({})", tempPath);
				Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
				return subPath + File.separator + targetPath.getFileName().toString();
			}

			// 여기까지 왔다면, 잘못된 것임.
			throw new RuntimeException("temp 파일 정보가 없습니다. 확인해 주세요.");
		} catch (Exception e) {
			throw e;
		}
	}

	public FiledownloadInfo loadAsResource(String filename, String... path) throws IOException {
		String[] targetPath = new String[path.length];
		for (int i = 0; i < path.length; i++) {
			targetPath[i] = FileUtils.getFilteredPath(path[i]);
		}
		Path file = Paths.get(uploadRootPath, targetPath);
		FiledownloadInfo fdi = new FiledownloadInfo();
		Resource resource = new UrlResource(file.toUri());
		if (resource.exists() || resource.isReadable()) {
			fdi.setResource(resource);
			fdi.setSize(resource.contentLength());
			fdi.setFilename(filename);
			return fdi;
		} else {
			throw new RuntimeException("Could not read file: " + path);
		}
	}

	public ResponseEntity<Resource> download(HttpServletRequest request, FiledownloadInfo record) {
//		String browser = FileUtils.getBrowser(request);
//		String originalFileName = record.getFilename();
//		String encodedFileName = FileUtils.encodeFilename(originalFileName, browser);
		String encodedFileName = URLEncoder.encode(record.getFilename(), StandardCharsets.UTF_8).replace("+", "%20");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        // 다운로드 되거나 로컬에 저장되는 용도로 쓰이는지를 알려주는 헤더
		headers.setContentDisposition(ContentDisposition.builder("attachment").filename(encodedFileName).build());

		return new ResponseEntity<>(record.getResource(), headers, HttpStatus.OK);
	}

}
