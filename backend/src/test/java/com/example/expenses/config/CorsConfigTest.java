package com.example.expenses.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String PROD_ORIGIN = "https://expense-management-system-phi.vercel.app";

    @Test
    void optionsPreflight_fromAllowedOrigin_returnsCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/expenses")
                        .header("Origin", PROD_ORIGIN)
                        .header("Access-Control-Request-Method", "GET")
                        // Include requested headers so the response will contain Access-Control-Allow-Headers
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", PROD_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Methods", Matchers.containsString("GET")))
                .andExpect(header().string("Access-Control-Allow-Headers", Matchers.anyOf(Matchers.containsString("Content-Type"), Matchers.containsString("content-type"))))
                ;
    }

    @Test
    void getRequest_fromAllowedOrigin_includesCorsHeader_evenOnErrorResponses() throws Exception {
        // Request a non-existent expense to trigger a 404 error response from controller
        mockMvc.perform(get("/api/expenses/999")
                        .header("Origin", PROD_ORIGIN))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Access-Control-Allow-Origin", PROD_ORIGIN));
    }

    @Test
    void getRequest_fromDisallowedOrigin_doesNotIncludeCorsHeader() throws Exception {
        mockMvc.perform(get("/api/expenses")
                        .header("Origin", "http://malicious.example.com"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
