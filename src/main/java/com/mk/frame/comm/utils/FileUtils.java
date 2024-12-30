package com.mk.frame.comm.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public static String getFilteredPath(String aString) {
		if (aString == null) {
			return null;
		}

		StringBuilder cleanString = new StringBuilder();
		for (int i = 0; i < aString.length(); ++i) {
			cleanString.append(FileUtils.cleanChar(aString.charAt(i)));
		}

		return cleanString.toString();
	}

	static char cleanChar(char aChar) {
		// 0 - 9
		for (int i = 48; i < 58; ++i) {
			if (aChar == i) {
				return (char) i;
			}
		}

		// 'A' - 'Z'
		for (int i = 65; i < 91; ++i) {
			if (aChar == i) {
				return (char) i;
			}
		}

		// 'a' - 'z'
		for (int i = 97; i < 123; ++i) {
			if (aChar == i) {
				return (char) i;
			}
		}

		// other valid characters
		return FileUtils.getSpecialLetter(aChar);
	}

	public static char getSpecialLetter(char aChar) {
		switch (aChar) {
            case '\\':
                return '\\';
            case '/':
                return '/';
            case '.':
                return '.';
            case '-':
                return '-';
            case '_':
                return '_';
            case ' ':
                return ' ';
            case ':':
                return ':';
            case '&':
                return '&';
            default:
                return '%';
		}
	}

	public static String encodeFilename(String filename, String browser) throws IOException {
		String encodedFilename = null;
		if (browser.equals("MSIE")) {
			encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
		} else if (browser.equals("Firefox")) {
			encodedFilename = "\"" + new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\"";
		} else if (browser.equals("Opera")) {
			encodedFilename = "\"" + new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\"";
		} else if (browser.equals("Chrome")) {
			encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
		} else {
			throw new RuntimeException("Not supported browser");
		}
		return encodedFilename;
	}

	public static String getBrowser(HttpServletRequest request) {
		String header = request.getHeader("User-Agent");
		if (header.contains("MSIE") || header.contains("Trident")) {
			return "MSIE";
		} else if (header.contains("Chrome")) {
			return "Chrome";
		} else if (header.contains("Opera")) {
			return "Opera";
		}
		return "Firefox";
	}
}
