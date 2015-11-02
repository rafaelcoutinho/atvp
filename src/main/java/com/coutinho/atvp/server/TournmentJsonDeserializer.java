package com.coutinho.atvp.server;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.coutinho.atvp.entities.Tournament;

public class TournmentJsonDeserializer extends JsonDeserializer<Tournament> {

	@Override
	public Tournament deserialize(JsonParser jsonParser,
			DeserializationContext ctx) throws IOException,
			JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);
		Tournament t = new Tournament(node.get("idManager").asLong(), node.get(
				"name").asText());
		t.setNumberOfRounds(node.get("rounds").asInt());
		System.out.println(node.toString());
		return t;
	}

}
