package com.geocoding.geocoder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RestApiSpringTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void can_geocode_using_spring_controller() throws Exception {
        mockMvc.perform(get("/api/geocode")
                .param("address", GoogleGeocodeRouteTest.KNOWN_ADDRESS))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.GeocodeResponse.status", equalTo("OK")));


    }
}
