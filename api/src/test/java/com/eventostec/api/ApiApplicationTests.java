package com.eventostec.api;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(true);
    }

    @Test
   public void main() {
      ApiApplication.main(new String[] {});
   }
}
