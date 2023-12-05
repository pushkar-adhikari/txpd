package info.pushkaradhikari.txpd.core.business.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import info.pushkaradhikari.txpd.core.business.error.RestResponseErrorHandler;
import info.pushkaradhikari.txpd.core.mapper.JacksonObjectMapper;

@Configuration
@ComponentScan(basePackages = "info.pushkaradhikari")
public class RestConfig {

	@Bean
    @Autowired
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        RestTemplate restTemplate;
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new JacksonObjectMapper());
        restTemplate = restTemplateBuilder.additionalMessageConverters(converter)
                .errorHandler(new RestResponseErrorHandler())
                .build();
        return restTemplate;
    }
}
