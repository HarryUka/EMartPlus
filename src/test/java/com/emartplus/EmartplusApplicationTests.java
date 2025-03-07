package com.emartplus;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = EmartplusApplication.class)
@ActiveProfiles("test")
class EmartplusApplicationTests {

    @Test
    void contextLoads() {
    }
} 