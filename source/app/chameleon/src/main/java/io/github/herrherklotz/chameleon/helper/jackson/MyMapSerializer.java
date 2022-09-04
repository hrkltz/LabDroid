package io.github.herrherklotz.chameleon.helper.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.github.herrherklotz.chameleon.Chameleon.LOG_D;

public class MyMapSerializer extends StdSerializer<HashMap<String, Object>> {
	protected MyMapSerializer(JavaType type) {
		super(type);
	}
	
	@Override
	public void serialize(HashMap<String, Object> value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
		//generator.writeStartArray();
		/*value.forEach((key, value1) -> {
			generator.writeStartObject();
			generator.writeFieldName(key);
			generator.writeObjectField(key, value);
			generator.writeEndObject();
		});*/
		generator.writeStartObject();
		for (Map.Entry<String, Object> entry : value.entrySet()){
			LOG_D("MyMapSerializer", "serialize", entry.getKey());
			//generator.writeStartObject();
			//generator.writeFieldName(entry.getKey());
			generator.writeObjectField(entry.getKey(), entry.getValue());
			//generator.writeEndObject();
		}
		generator.writeEndObject();
		//generator.writeEndArray();
	}
}