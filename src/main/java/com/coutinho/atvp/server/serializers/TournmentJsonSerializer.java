package com.coutinho.atvp.server.serializers;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.coutinho.atvp.entities.Tournament;

public class TournmentJsonSerializer extends JsonSerializer<Tournament> {

	@Override
	public void serialize(Tournament tourn, JsonGenerator jgen,
			SerializerProvider arg2) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField("name", tourn.getName());
		jgen.writeNumberField("rounds", tourn.getNumberOfRounds());
		jgen.writeNumberField("idManager", tourn.getIdManager());
		jgen.writeEndObject();

	}

}
