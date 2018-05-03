package com.systrangroup.unkown.app.controller;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.systrangroup.unkown.app.handler.FileService;

@RestController
@RequestMapping("/")
@CrossOrigin
public class FileContorller {
	@Autowired
	private FileService service;
	
	/**
	 */
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<?> uploadFile(@RequestParam("uploadfile") MultipartFile[] uploadfiles) {
		return service.fileUpload(uploadfiles);
	}
	
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "/fileSend", method = RequestMethod.POST)
	public void sendFile(@RequestParam("uploadfile") MultipartFile[] uploadfiles) {
		service.fileSend(uploadfiles);
	}
	
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="/binarySend", method = RequestMethod.POST)
	public void binarySend(@RequestParam("uploadfile") MultipartFile[] uploadFile) {
		service.binarySend(uploadFile);
	}
	
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="/binaryUpload", method = RequestMethod.POST)
	public void binaryUpload(@RequestBody String jsonFileData) {
		service.binaryToFile(jsonFileData);
	}
}
