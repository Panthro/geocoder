package com.geocoding.geocoder.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.XmlJsonDataFormat;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.processor.validation.PredicateValidationException;

import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.INFO;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;


public class GeocodeApiRoute extends RouteBuilder {


    public static final String GOOGLE_GEOCODE_ROUTE = "direct:googleGeocodingApi";
    public static final String ADDRESS_HEADER = "address";

    private final String apiKey;
    private final Integer serverPort;
    private final XmlJsonDataFormat xmlJsonDataFormat;

    public GeocodeApiRoute(CamelContext context, int serverPort, String apiKey, XmlJsonDataFormat xmlJsonDataFormat) {
        super(context);
        notNull(context, "Camel context cannot be null");
        hasText(apiKey, "Api Key cannot be empty");
        notNull(xmlJsonDataFormat, "The Xml -> Json formatter cannot be null");
        this.serverPort = serverPort;
        this.apiKey = apiKey;
        this.xmlJsonDataFormat = xmlJsonDataFormat;
    }

    @Override
    public void configure() {

        onException(PredicateValidationException.class)
                .handled(true)
                .setBody().simple("{\"status\":\"error\", \"message\":\"address must be defined\"}")
                .setHeader(HTTP_RESPONSE_CODE, constant(400))
                .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON_VALUE));


        // Configure the api docs, so we can load it up with swaqgger-ui
        restConfiguration()
                .contextPath("/camel")
                .host("localhost")
                .port(serverPort)
                .enableCORS(true)
                .apiContextPath("api-docs")
                .apiProperty("api.title", "Geocode REST api")
                .apiProperty("api.version", "v1")
                .apiContextRouteId("doc-api")
                .component("servlet");

        // Configure our geocode endpoint, so it will get exposed on swagger docs
        rest("/geocode")
                .id("geocode-api")
                .description("Geocoding api, convert formatted address into geoaddress")
                .produces(APPLICATION_JSON_VALUE)
                .get()
                .param()
                .name(ADDRESS_HEADER)
                .required(true)
                .description("The formatted address")
                .type(RestParamType.query)
                .endParam()
                .responseMessage().code(200).message("A JSON containing the Geocoded address").endResponseMessage()
                .responseMessage().code(400).message("If the required address are not passed").endResponseMessage()
                .responseMessage().code(500).message("When an internal error occur, or the third part api does stop working").endResponseMessage()
                //TODO Better error handling when validation fails, throwing an exception is too ugly
                .to(GOOGLE_GEOCODE_ROUTE);


        // Configure the google api geocode route, so we can forward valid requests to it
        from(GOOGLE_GEOCODE_ROUTE)
                .streamCaching()
                .routeId("google-geocode")
                .log(INFO, ">>> Address: ${header.address}")
                .validate().simple("${header.address} != null")
                .setHeader(Exchange.HTTP_QUERY, simple("?address=${header.address}&key=" + apiKey))
                //This one was tricky! Trying out with chrome was forwarding this header and messing up with everything
                .removeHeader("accept-encoding")
                .to("https://maps.googleapis.com/maps/api/geocode/xml?throwExceptionOnFailure=false&bridgeEndpoint=true")
                .log(INFO, "<<< Result XML: ${body}")
                .marshal(xmlJsonDataFormat)
                .log(INFO, "<<< Result JSON: ${body}")
                .convertBodyTo(String.class)
                .setHeader(HTTP_RESPONSE_CODE, constant(200))
                .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON_VALUE))
        ;


    }
}
