package bcp.spring.jackson;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

@Component
@Slf4j
public class PropertyAnnotatedFilter extends SimpleBeanPropertyFilter {

	@Override
	protected boolean include(BeanPropertyWriter writer) {
		return true;
	}

	@Override
	protected boolean include(PropertyWriter writer) {
		return true;
	}

	@Override
	public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer)
			throws Exception {
		// ignore all the fields that are not annotated with JsonProperty
		if (writer.getAnnotation(JsonProperty.class) != null) {
			super.serializeAsField(pojo, jgen, provider, writer);
		}
	}

	@Autowired
	ObjectMapper objectMapper; 

	@PostConstruct
	public void registerFilter() {
		objectMapper.setFilterProvider(new SimpleFilterProvider().addFilter("propertyAnnotatedFilter", this));
		log.info("PropertyAnnotatedFilter registered with object mapper");
	}
}