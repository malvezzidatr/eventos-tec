package com.eventostec.api.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.repositories.EventRepository;

@Service
public class EventService {
	@Autowired
	private AmazonS3 s3Client;

	@Value("${aws.bucket.name}")
    private String bucketName;

	@Autowired
	private EventRepository eventRepository;

	private static final Logger logger = LoggerFactory.getLogger(EventService.class);


	public Event createEvent(EventRequestDTO data) throws IOException {
		logger.info("Start - EventService - createEvent - title: {}", data.title());
		String imgUrl = null;
		
		if(data.image() != null) {
			imgUrl = this.uploadImg(data.image());
		}

		Event newEvent = new Event();
		newEvent.setTitle(data.title());
		newEvent.setDescription(data.description());
		newEvent.setEventUrl(data.eventUrl());
		newEvent.setData(new Date(data.date()));
		newEvent.setImgUrl(imgUrl);
		newEvent.setRemote(data.remote());

		eventRepository.save(newEvent);
		logger.info("End - EventService - createEvent - title: {}", data.title());

		return newEvent;
	}
	
	private String uploadImg(MultipartFile multipartFile) throws IOException {
		String fileName = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();
		logger.info("Start - EventService - uploadImg - upload file name: {}", fileName);

		try {
			File file = this.convertMultipartFile(multipartFile);
			s3Client.putObject(bucketName, fileName, file);
			file.delete();

			String s3Url = s3Client.getUrl(bucketName, fileName).toString();
			logger.info("End - EventService - uploadImg - upload file name: {}, s3 url", fileName, s3Url);
			return s3Url;
		} catch (IOException e) {
			logger.error("End - EventService - uploadImg - upload file name: {}", fileName);
			throw e;
		}
	}

	private File convertMultipartFile(MultipartFile multipartFile) throws IOException {
		logger.info("Start - EventService - convertMultipartFile - upload file name: {}", multipartFile.getOriginalFilename());

		try {
			File convFile = new File(multipartFile.getOriginalFilename());
			FileOutputStream fos = new FileOutputStream(convFile);
			fos.write(multipartFile.getBytes());
			fos.close();
			logger.info("End - EventService - convertMultipartFile - upload file name: {}", multipartFile.getOriginalFilename());
			return convFile;
		} catch (IOException e) {
			logger.error("End - EventService - convertMultipartFile - upload file name: {}", multipartFile.getOriginalFilename());
			throw e;
		}
	}
}
