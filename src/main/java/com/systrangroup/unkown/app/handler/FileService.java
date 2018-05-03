package com.systrangroup.unkown.app.handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.HttpUrl;
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
	@Value("${mutipart.file.path}")
	private String saveFilePath;
	
	public ResponseEntity<?> fileUpload(MultipartFile[] uploadfiles) {
		File path = new File(saveFilePath);

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
	
	public void fileSend(MultipartFile[] uploadfiles) {
		try {
			OkHttpClient client = new OkHttpClient();
			Builder builder = new Request.Builder().url("http://192.168.0.22:8080/fileUpload");
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
	
	public void binarySend(MultipartFile[] uploadfiles) {
		try {
			Gson gson = new Gson();
			JsonArray jsonArray = new JsonArray();
			
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
				byte[] binary = Files.readAllBytes(createFile.toPath());
				
				JsonObject json = new JsonObject();
				json.addProperty("name", createFile.getName());
				json.addProperty("file", binary + "");
				jsonArray.add(json);
			}
			
			OkHttpClient client = new OkHttpClient();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, gson.toJson(jsonArray));
			Request request = new Request.Builder().url("http://192.168.0.11:8080/binaryUpload").post(body).build();
			
			client.newCall(request).execute();
		} catch(IOException e) {
			log.warning(e.getMessage());
		}
	}
	
	public void binaryToFile(String jsonFileData) {
		JsonParser jsonParser = new JsonParser();
		JsonElement jsonElement = jsonParser.parse(jsonFileData);
		JsonArray jsonArray = jsonElement.getAsJsonArray();
		jsonArray.forEach(json -> {
			byte[] fileData = json.getAsJsonObject().get("file").getAsString().getBytes();
			String fileName = json.getAsJsonObject().get("name").getAsString();
			
			File file = new File("/Users/min/Documents/multipart-test-workspace/" + fileName);
			if (!file.exists()) {
				file.mkdirs();
			}
			try {
				Files.write(file.toPath(), fileData, StandardOpenOption.WRITE);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
