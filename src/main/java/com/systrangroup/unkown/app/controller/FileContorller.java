package com.systrangroup.unkown.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
	public @ResponseBody ResponseEntity<?> uploadFile(@RequestParam("uploa dfile") MultipartFile[] uploadfiles) {
		return service.fileUpload(uploadfiles);
	}
	
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "/fileSend", method = RequestMethod.GET)
	public void sendFile() {
		
	}
}
