package com.geocoding.geocoder;

import com.geocoding.geocoder.routes.GeocodeApiRoute;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.dataformat.XmlJsonDataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GeocoderApplication {

    @Value("${server.port:8080}")
    private Integer serverPort;
    @Value("${google.api.key}")
    private String googleApiKey;

    public static void main(String[] args) {
        SpringApplication.run(GeocoderApplication.class, args);
    }


    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(), "/*");
        servlet.setName("CamelServlet");
        return servlet;
    }

    @Bean
    public RouteBuilder camelRestApi(CamelContext camelContext) {
        return new GeocodeApiRoute(camelContext, serverPort, googleApiKey, xmlJsonDataFormat());
    }

    @Bean
    public XmlJsonDataFormat xmlJsonDataFormat() {
        final XmlJsonDataFormat xmlJsonDataFormat = new XmlJsonDataFormat();
        xmlJsonDataFormat.setForceTopLevelObject(true);
        return xmlJsonDataFormat;
    }
}
