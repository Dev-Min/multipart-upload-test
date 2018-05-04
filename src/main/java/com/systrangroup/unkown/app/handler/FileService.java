package com.systrangroup.unkown.app.handler;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.tomcat.util.codec.binary.Base64;
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
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

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
				try (
						OutputStream out = new FileOutputStream(path.getAbsolutePath() + File.separator + uploadFile.getOriginalFilename());
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
	
	public void binarySend(MultipartFile[] uploadfiles) {
		try {
			Gson gson = new Gson();
			JsonArray jsonArray = new JsonArray();
			
			log.info("Upload File Size : " + uploadfiles.length);
			
			for (MultipartFile file : uploadfiles) {
				String fileName = file.getOriginalFilename();
				log.info(fileName);
				String path = saveFilePath + fileName;
				@Cleanup OutputStream out = new FileOutputStream(path);
				@Cleanup BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
				
				byte[] buffer = new byte[1024];
				int read;
				while ((read = bis.read(buffer)) > 0) {
					out.write(buffer, 0, read);
				}
				
				File tgtFile = new File(path);
				Optional<String> result = fileToString(tgtFile);
				if (result.isPresent()) {
					String binary = result.get();
					JsonObject json = new JsonObject();
					json.addProperty("name", fileName);
					json.addProperty("file", binary + "");
					jsonArray.add(json);
					log.info("Create Json Data");
				}
			}
			
			OkHttpClient client = new OkHttpClient();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, gson.toJson(jsonArray));
			Request request = new Request.Builder().url(address + "binaryUpload").post(body).build();
			
			log.info("Start sending binary file");
			client.setConnectTimeout(10, TimeUnit.MINUTES);
			client.newCall(request).enqueue(new Callback() {
				@Override
				public void onResponse(Response response) throws IOException {
					log.info("Binary file sending complete");
				}
				
				@Override
				public void onFailure(Request request, IOException e) {
					log.warning(e.getMessage());
				}
			});
		} catch(IOException e) {
			log.warning(e.getMessage());
		}
	}
	
	public ResponseEntity<?> binaryToFile(String jsonFileData) {
		JsonParser jsonParser = new JsonParser();
		JsonElement jsonElement = jsonParser.parse(jsonFileData);
		JsonArray jsonArray = jsonElement.getAsJsonArray();
		if (!jsonArray.isJsonNull()) {
			jsonArray.forEach(json -> {
				String fileData = json.getAsJsonObject().get("file").getAsString();
				String fileName = json.getAsJsonObject().get("name").getAsString();
				File file = new File(saveFilePath);
				if (!file.exists()) {
					file.mkdirs();
				}
				
				log.info(fileName + " file create");
				stringToFile(fileData, fileName);
				log.info(fileName + " file create complete");
			});
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	private Optional<String> fileToString(File tgtFile) {
		Optional<String> result = Optional.empty();
		try (
				FileInputStream fis = new FileInputStream(tgtFile);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();) {

			int length = 0;
			byte[] buf = new byte[1024];
			while((length = fis.read(buf)) > 0) {
				baos.write(buf, 0, length);
			}
			
			byte[] fileArray = baos.toByteArray();
			result = Optional.of(new String(Base64.encodeBase64(fileArray)));
	        
		} catch (IOException e) {
			log.info(e.getMessage());
		}
		
		return result;
	}
	
	private void stringToFile(String res, String name) {
		File resultFile = new File(saveFilePath + name);
        try (FileOutputStream fos = new FileOutputStream(resultFile)) {
	        byte[] fileData = Base64.decodeBase64(res);
	        fos.write(fileData);
        } catch(IOException e) {
			log.info(e.getMessage());
		} finally {
			log.info(resultFile.getName() + " create : " + resultFile.exists());
		}
	}
}
