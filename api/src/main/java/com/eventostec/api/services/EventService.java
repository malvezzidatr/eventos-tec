package com.eventostec.api.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.coupon.Coupon;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventDetailsDTO;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.domain.event.PaginatedResponse;
import com.eventostec.api.repositories.EventRepository;

@Service
public class EventService {
	@Autowired
	private AmazonS3 s3Client;

	@Value("${aws.bucket.name}")
    private String bucketName;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private AddressService addressService;

	@Autowired
	private CouponService couponService;

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

		if(!data.remote()) {
			this.addressService.createAddress(data, newEvent);
		}
		logger.info("End - EventService - createEvent - title: {}", data.title());

		return newEvent;
	}

	public PaginatedResponse<EventResponseDTO> getUpcomingEvents(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Event> events = eventRepository.findUpComingEvents(new Date(), pageable);
		List<EventResponseDTO> eventResponseDTOs = events.map(event -> new EventResponseDTO(
																event.getId(), 
																event.getTitle(), 
																event.getDescription(), 
																event.getData(), 
																event.getAddress() != null ? event.getAddress().getCity() : "", 
																event.getAddress() != null ? event.getAddress().getUf() : "", 
																event.getRemote(), 
																event.getImgUrl(), 
																event.getEventUrl()
															)).stream().toList();
		int totalPage = events.getTotalPages();
		return new PaginatedResponse<>(eventResponseDTOs, totalPage);
	}
	
	public PaginatedResponse<EventResponseDTO> getFilteredEvents(int page, int size, String title, String city, String uf, Date startDate, Date endDate) {
		title = (title != null) ? title : "";
		city = (city != null) ? city : "";
		uf = (uf != null) ? uf : "";
		startDate = (startDate != null) ? startDate : new Date(0);
		endDate = (endDate != null) ? endDate : new Date();

		Pageable pageable = PageRequest.of(page, size);
		Page<Event> events = this.eventRepository.findFilteredEvents(title, city, uf, startDate, endDate, pageable);

		List<EventResponseDTO> eventResponseDTOs = events.map(event -> new EventResponseDTO(
			event.getId(), 
			event.getTitle(), 
			event.getDescription(), 
			event.getData(), 
			event.getAddress() != null ? event.getAddress().getCity() : "", 
			event.getAddress() != null ? event.getAddress().getUf() : "", 
			event.getRemote(), 
			event.getImgUrl(), 
			event.getEventUrl()
		)).stream().toList();

		int totalPage = events.getTotalPages();
		return new PaginatedResponse<>(eventResponseDTOs, totalPage);
	};

	public PaginatedResponse<EventResponseDTO> getOlderEvents(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Event> events = eventRepository.findOlderEvents(new Date(), pageable);
		List<EventResponseDTO> eventResponseDTOs = events.map(event -> new EventResponseDTO(
			event.getId(), 
			event.getTitle(), 
			event.getDescription(), 
			event.getData(), 
			event.getAddress() != null ? event.getAddress().getCity() : "", 
			event.getAddress() != null ? event.getAddress().getUf() : "", 
			event.getRemote(), 
			event.getImgUrl(), 
			event.getEventUrl()
		)).stream().toList();

		int totalPage = events.getTotalPages();

        return new PaginatedResponse<>(eventResponseDTOs, totalPage);
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

    public EventDetailsDTO getEventDetails(UUID eventId) {
		Event event = this.eventRepository.findById(eventId)
				.orElseThrow(() -> new IllegalArgumentException("Event not found"));

		List<Coupon> coupons = this.couponService.consultCoupons(eventId, new Date());

		List<EventDetailsDTO.CouponDTO> couponDTOs = coupons.stream()
														.map(coupon -> new EventDetailsDTO.CouponDTO(
															coupon.getCode(),
															coupon.getDiscount(),
															coupon.getValid()))
														.collect(Collectors.toList());

		return new EventDetailsDTO(
			event.getId(),
			event.getTitle(),
			event.getDescription(),
			event.getData(),
			event.getAddress() != null ? event.getAddress().getCity() : "",
			event.getAddress() != null ? event.getAddress().getUf() : "",
			event.getImgUrl(),
			event.getEventUrl(),
			couponDTOs);
    }
}
