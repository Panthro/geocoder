package com.geocoding.geocoder;

import org.apache.camel.CamelContext;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.DisableJmx;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.InputStream;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = GeocoderApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx
public class RestApiRouteTest {

    public static final String ENDPOINT_URI = "http://localhost:8080/camel/geocode";

    @Autowired
    private CamelContext camelContext;

    @Test
    @DirtiesContext
    public void redirects_request_to_google_api() throws Exception {


        Object jsonResult = camelContext.createProducerTemplate().requestBodyAndHeader(ENDPOINT_URI, null, "address", GoogleGeocodeRouteTest.KNOWN_ADDRESS);
        final String json = IOUtils.toString((InputStream) jsonResult);

        assertThat(json, isJson());
    }

    @Test
    public void address_is_required() {

        assertThatThrownBy(() -> camelContext.createProducerTemplate().requestBody(ENDPOINT_URI, (Object) null))
                .hasCauseInstanceOf(HttpOperationFailedException.class);

    }


}
