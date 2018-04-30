package com.systrangroup.unkown.app.handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.java.Log;

@Log
@Service
public class FileService {
	@Value("${mutipart.file.path}")
	private String saveFilePath;
	
	public ResponseEntity<?> fileUpload(MultipartFile[] uploadfiles) {
		File path = new File("");

		if (uploadfiles != null && uploadfiles.length > 0) {
			for (MultipartFile uploadFile : uploadfiles) {
				try (OutputStream out = new FileOutputStream(path.getAbsolutePath() + File.separator + uploadFile.getOriginalFilename());
						BufferedInputStream bis = new BufferedInputStream(uploadFile.getInputStream());) {
					log.info("File Name : " + uploadFile.getOriginalFilename());
					// Save the file locally
					byte[] buffer = new byte[1024];
					int read;
					while ((read = bis.read(buffer)) > 0) {
						out.write(buffer, 0, read);
					}
				}
				catch (IOException e) {
					log.info(e.getMessage());
					return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				}
			}
			return new ResponseEntity<>(HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	public void fileSend() {
		
	}
}
