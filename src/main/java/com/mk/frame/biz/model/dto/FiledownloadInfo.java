package com.mk.frame.biz.model.dto;

import lombok.Data;
import org.springframework.core.io.Resource;

@Data
public class FiledownloadInfo {
    private String filename;
	private Resource resource;
	private long size;
}
