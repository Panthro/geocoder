package com.geocoding.geocoder.rest;

import com.geocoding.geocoder.routes.GeocodeApiRoute;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geocode")
public class GeocodeController {

    private final ProducerTemplate producer;

    private final CamelContext camelContext;

    @Autowired
    public GeocodeController(ProducerTemplate producer, CamelContext camelContext) {
        this.producer = producer;
        this.camelContext = camelContext;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> geocodeAddress(@RequestParam String address) {
        final Exchange requestExchange = ExchangeBuilder.anExchange(camelContext).withHeader(GeocodeApiRoute.ADDRESS_HEADER, address).build();
        final Exchange responseExchange = producer.send(GeocodeApiRoute.GOOGLE_GEOCODE_ROUTE, requestExchange);
        final String responseBody = responseExchange.getIn().getBody(String.class);
        final int responseCode = responseExchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        return ResponseEntity.status(responseCode).body(responseBody);
    }
}
