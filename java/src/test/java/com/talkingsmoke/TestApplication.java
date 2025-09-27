package com.talkingsmoke;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Minimal test-only Spring Boot configuration to allow tests to start a context.
 */
@SpringBootConfiguration
@ComponentScan(basePackages = "com.talkingsmoke")
public class TestApplication {
    // Intentionally empty; only used for test bootstrap
}
