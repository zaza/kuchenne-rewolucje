package com.github.zaza.kuchennerewolucje;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.geojson.FeatureCollection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.maps.GeoApiContext;

public class Geocoder {

	private static GeoApiContext CONTEXT;

	public static void main(String[] args) throws Exception {
		CONTEXT = createContext();
		FeatureCollection featureCollection = new FeatureCollection();
		EpisodesReader.readAllEpisodes().values().stream() //
				.map(new EpisodeToFeatureMapper(CONTEXT)) //
				.filter(f -> f.isPresent()) //
				.forEach(f -> featureCollection.add(f.get()));
		createObjectWriter().writeValue(new File("data.geojson"), featureCollection);
	}

	private static ObjectWriter createObjectWriter() {
		return new ObjectMapper().writer().with(SerializationFeature.INDENT_OUTPUT)
				.without(SerializationFeature.WRITE_NULL_MAP_VALUES);
	}

	private static GeoApiContext createContext() throws IOException {
		return new GeoApiContext().setApiKey(new String(Files.readAllBytes(Paths.get("api-key")), "UTF-8"));
	}
}
