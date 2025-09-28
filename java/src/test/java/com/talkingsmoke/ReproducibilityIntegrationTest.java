package com.talkingsmoke;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
public class ReproducibilityIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void samePayloadProducesSameOutput() throws Exception {
        String payload = "{\"input\":[0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0]}";

        MvcResult r1 = mvc.perform(post("/infer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult r2 = mvc.perform(post("/infer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        String s1 = r1.getResponse().getContentAsString();
        String s2 = r2.getResponse().getContentAsString();

        assertThat(s1).isEqualTo(s2);
    }
}
