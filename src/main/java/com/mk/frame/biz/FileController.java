package com.mk.frame.biz;

import com.mk.frame.biz.model.dto.FiledownloadInfo;
import com.mk.frame.biz.model.dto.FileuploadInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "File API")
@Slf4j
@RequestMapping(path = "/api/v1/file")
@Controller
public class FileController {
    @Autowired
	private FileStorageService fileStorageService;

	@Operation(summary = "첨부파일 업로드", description = "첨부파일을 업로드한다.")
	@PostMapping(path = "/attach", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<FileuploadInfo> attachedFileUpload(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "etcInfo", required = false) String etcInfo
	) throws IOException {
		FileuploadInfo stored = fileStorageService.storeToTempDir(file, etcInfo);
		log.info("=== stored : {}", stored);

		return ResponseEntity.ok(stored);
	}

	@Operation(summary = "첨부파일 다운로드", description = "첨부파일을 다운로드한다.")
	@GetMapping("/attach/{path1}/{path2}")
	public ResponseEntity<?> attachedFileDownload(
            HttpServletRequest request, HttpServletResponse response,
            @PathVariable("path1") String path1,
			@PathVariable("path2") String path2,
            @RequestParam(name = "name", required = false) String name
    ) throws IOException {
		log.info("=== file download : {}/{} ({})", path1, path2, name);
		if (Strings.isBlank(name)) {
			name = "nobody";
		} else {
			name = name.trim();
		}
		FiledownloadInfo fdi = fileStorageService.loadAsResource(name, path1, path2);

		try {
			return fileStorageService.download(request, fdi);
		} catch (Exception e) {
			log.error(e.getMessage());
            throw new RuntimeException("Error occurred while downloading file", e);
		}
	}
}