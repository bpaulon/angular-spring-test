package bcp.spring.angularjs.userhello;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


@Configuration
public class WebApplicationConfig extends WebMvcConfigurerAdapter {

	@Bean
	@Primary
	public ObjectMapper jsonMapper() {
		ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().indentOutput(true)
				.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.build();

		return mapper;
	}

}
