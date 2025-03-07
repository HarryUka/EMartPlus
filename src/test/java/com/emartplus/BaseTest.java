package com.emartplus;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = EmartplusApplication.class)
@ActiveProfiles("test")
public abstract class BaseTest {
    // Common test utilities can be added here
} 