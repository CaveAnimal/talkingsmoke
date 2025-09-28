package com.talkingsmoke;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
public class InferControllerValidationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    public void malformedJsonReturnsBadRequest() throws Exception {
        String bad = "{\"input\": [0.1, 0.2,}"; // trailing comma -> malformed
        mvc.perform(post("/infer").contentType(MediaType.APPLICATION_JSON).content(bad))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void emptyArrayReturnsBadRequest() throws Exception {
        String payload = "{\"input\": []}"; // empty array triggers invalid-input-dimensions
        mvc.perform(post("/infer").contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isBadRequest());
    }
}
