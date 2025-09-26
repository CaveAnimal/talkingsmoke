// ...existing code...
package com.talkingsmoke;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TalkingSmokeApplicationTests {

    @Autowired
    ApplicationContext context;

    @Test
    void contextLoads() {
        // verify that the Spring application context starts
        assertThat(context).isNotNull();
    }
}
// ...existing code...

