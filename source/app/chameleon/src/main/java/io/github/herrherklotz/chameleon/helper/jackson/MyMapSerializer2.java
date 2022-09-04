package io.github.herrherklotz.chameleon.helper.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.github.herrherklotz.chameleon.x.backend.system.project.Port;


public class MyMapSerializer2 extends StdSerializer<HashMap<String, Port>> {
	protected MyMapSerializer2(JavaType type) {
		super(type);
	}
	
	@Override
	public void serialize(HashMap<String, Port> value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
		generator.writeStartObject();

		for (Map.Entry<String, Port> entry : value.entrySet()){
			generator.writeObjectField(entry.getKey(), entry.getValue());
		}

		generator.writeEndObject();
	}
}