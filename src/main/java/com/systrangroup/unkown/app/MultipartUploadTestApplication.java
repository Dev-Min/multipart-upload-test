package com.systrangroup.unkown.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class MultipartUploadTestApplication extends SpringBootServletInitializer {
	
	@Override
	  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
	    return application.sources(MultipartUploadTestApplication.class);
	  }

	public static void main(String[] args) {
		SpringApplication.run(MultipartUploadTestApplication.class, args);
	}
}
