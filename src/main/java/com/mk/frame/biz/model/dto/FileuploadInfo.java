package com.mk.frame.biz.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class FileuploadInfo {
    private String path;
	private String filename;
	private long fileSize;
	private String fileExtension;
	private String etcInfo;

	@Builder
	public FileuploadInfo(String path, String filename, long fileSize, String fileExtension, String etcInfo) {
		super();
		this.path = path;
		this.filename = filename;
		this.fileSize = fileSize;
		this.fileExtension = fileExtension;
		this.etcInfo = etcInfo;
	}
}
