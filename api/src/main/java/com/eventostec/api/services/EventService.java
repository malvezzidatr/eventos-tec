package com.eventostec.api.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;

@Service
public class EventService {
	@Autowired
	private AmazonS3 s3Client;

	@Value("${aws.bucket.name}")
    private String bucketName;

	public Event createEvent(EventRequestDTO data) throws IOException {
		String imgUrl = null;
		
		if(data.image() != null) {
			imgUrl = this.uploadImg(data.image());
		}
		
		Event newEvent = new Event();
		newEvent.setTitle(data.title());
		newEvent.setDescription(data.description());
		newEvent.setEventUrl(data.eventUrl());
		newEvent.setDate(new Date(data.date()));
		newEvent.setImgUrl(imgUrl);

		return newEvent;
	}
	
	private String uploadImg(MultipartFile multipartFile) throws IOException {
		String fileName = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();

		try {
			File file = this.convertMultipartFile(multipartFile);
			s3Client.putObject(bucketName, fileName, file);
			file.delete();

			return s3Client.getUrl(bucketName, fileName).toString();
		} catch (IOException e) {
			throw e;
		}
	}

	private File convertMultipartFile(MultipartFile multipartFile) throws IOException {
		try {
			File convFile = new File(multipartFile.getOriginalFilename());
			FileOutputStream fos = new FileOutputStream(convFile);
			fos.write(multipartFile.getBytes());
			fos.close();
			return convFile;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
