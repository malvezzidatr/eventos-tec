package com.eventostec.api.services;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.eventostec.api.domain.address.Address;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.repositories.AddressRepository;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTests {
    @InjectMocks
    private AddressService addressService;

    @Mock
    private AddressRepository addressRepository;

    @Test
    void shouldCreateNewAddress() throws IOException {
        Address address = new Address();
        address.setCity("São Paulo");
        address.setUf("SP");
        address.setEvent(new Event());

        when(addressRepository.save(any(Address.class))).thenReturn(address);

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream imageStream = classLoader.getResourceAsStream("images/image-test.jpg");

        assertNotNull(imageStream);
        MultipartFile mockFile = new MockMultipartFile(
            "image", 
            "imagem-test.jpg", 
            "image/jpeg", 
            imageStream
        );

        EventRequestDTO eventRequestDTO = new EventRequestDTO("Test",
                                                            "Test",
                                                            1L,
                                                            "São Paulo",
                                                            "SP",
                                                            false,
                                                            "www.test.com.br",
                                                            mockFile);
        
        Address responseAddress = addressService.createAddress(eventRequestDTO, new Event());
        assertNotNull(responseAddress);
        assertEquals("São Paulo", address.getCity());
        assertEquals("SP", address.getUf());

    }
}
