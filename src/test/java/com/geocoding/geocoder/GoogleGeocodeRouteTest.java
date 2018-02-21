package com.geocoding.geocoder;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.DisableJmx;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;


@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx
public class GoogleGeocodeRouteTest {

    public static final String KNOWN_ADDRESS = "Jacob Bontiusplaats 9, 1018 Amsterdam, Netherlands";
    public static final String BAD_ADDRESS = "just a bad address";


    @Produce(uri = "direct:googleGeocodingApi")
    private ProducerTemplate googleGeocodingProducer;

    @Before
    public void setUp() throws Exception {
        googleGeocodingProducer.start();
    }

    @Test
    public void can_geocode_known_address() {

        final String jsonResult = String.valueOf(googleGeocodingProducer.requestBodyAndHeader(null, "address", KNOWN_ADDRESS));

        assertThat(jsonResult, isJson());

        assertThat(jsonResult, hasJsonPath("$.GeocodeResponse.status", equalTo("OK")));

        assertThat(jsonResult, hasJsonPath("$.GeocodeResponse.result.formatted_address", equalTo(KNOWN_ADDRESS)));

        assertThat(jsonResult, hasJsonPath("$.GeocodeResponse.result.geometry.location.lat", equalTo("52.3713577")));

        assertThat(jsonResult, hasJsonPath("$.GeocodeResponse.result.geometry.location.lng", equalTo("4.9278302")));
    }

    @Test
    public void can_handle_bad_address() {
        final String jsonResult = String.valueOf(googleGeocodingProducer.requestBodyAndHeader(null, "address", BAD_ADDRESS));
        assertThat(jsonResult, isJson());
        assertThat(jsonResult, hasJsonPath("$.GeocodeResponse[*]", hasItem("ZERO_RESULTS")));

    }


    @Test
    public void validation_error_on_empty_address() {
        final String jsonResult = String.valueOf(googleGeocodingProducer.requestBody(null));
        assertThat(jsonResult, isJson());
        assertThat(jsonResult, hasJsonPath("$.status", equalTo("error")));
        assertThat(jsonResult, hasJsonPath("$.message", equalTo("address must be defined")));

    }

}
