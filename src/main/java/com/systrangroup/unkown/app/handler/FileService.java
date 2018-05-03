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

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;

import lombok.Cleanup;
import lombok.extern.java.Log;

@Log
@Service
public class FileService {
	@Value("${multipart.file.path}")
	private String saveFilePath;
	
	@Value("${multipart.server.address}")
	private String address;
	
	public ResponseEntity<?> fileUpload(MultipartFile[] uploadfiles) {
		File path = new File(saveFilePath);

		if (uploadfiles != null && uploadfiles.length > 0) {
			for (MultipartFile uploadFile : uploadfiles) {
				try {
					@Cleanup OutputStream out = new FileOutputStream(path.getAbsolutePath() + File.separator + uploadFile.getOriginalFilename());
					@Cleanup BufferedInputStream bis = new BufferedInputStream(uploadFile.getInputStream());
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
	
	public void fileSend(MultipartFile[] uploadfiles) {
		try {
			OkHttpClient client = new OkHttpClient();
			Builder builder = new Request.Builder().url(address + "fileUpload");
			MediaType mediaType = MediaType.parse("multipart/from-data;");
			MultipartBuilder mBuilder = new MultipartBuilder();
			
			for (MultipartFile file : uploadfiles) {
				String path = saveFilePath + file.getOriginalFilename();
				@Cleanup OutputStream out = new FileOutputStream(path);
				@Cleanup BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
				
				byte[] buffer = new byte[1024];
				int read;
				while ((read = bis.read(buffer)) > 0) {
					out.write(buffer, 0, read);
				}
				
				File createFile = new File(path);
				mBuilder = mBuilder.addFormDataPart("uploadfile", createFile.getName(), RequestBody.create(mediaType, createFile));
			}
			builder.post(mBuilder.build());
			Request req = builder.build();
			
			client.newCall(req).execute();
		} catch (IOException e) {
			log.info(e.getMessage());
		}
	}
}
