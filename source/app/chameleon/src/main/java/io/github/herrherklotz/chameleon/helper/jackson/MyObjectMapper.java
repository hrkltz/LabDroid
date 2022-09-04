package io.github.herrherklotz.chameleon.helper.jackson;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import java.util.HashMap;
import io.github.herrherklotz.chameleon.x.backend.system.project.Port;


public class MyObjectMapper extends ObjectMapper {
	public MyObjectMapper() {
		JavaType myMapType = this.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
		JavaType myMapType2 = this.getTypeFactory().constructMapType(HashMap.class, String.class, Port.class);

		this.setFilterProvider(new SimpleFilterProvider().addFilter("myFilter", SimpleBeanPropertyFilter.serializeAll()));
		this.registerModule(new SimpleModule().addSerializer(new MyMapSerializer(myMapType)));
		this.registerModule(new SimpleModule().addSerializer(new MyMapSerializer2(myMapType2)));
	}
}
