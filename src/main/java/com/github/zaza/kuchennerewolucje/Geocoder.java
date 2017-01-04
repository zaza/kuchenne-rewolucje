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

	private static final File GEOJSON_FILE = new File("data.geojson");
	
	private static GeoApiContext CONTEXT;

	public static void main(String[] args) throws Exception {
		CONTEXT = createContext();
		FeatureCollection features = FeatureCollectionReader.read(GEOJSON_FILE);
		NotExistingFeaturePredicate notExisting = new NotExistingFeaturePredicate(features); 
		EpisodesReader.readAllEpisodes().values().stream() //
				.filter(notExisting)
				.map(new EpisodeToFeatureMapper(CONTEXT)) //
				.filter(f -> f.isPresent()) //
				.forEach(f -> features.add(f.get()));
		createObjectWriter().writeValue(GEOJSON_FILE, features);
	}

	private static ObjectWriter createObjectWriter() {
		return new ObjectMapper().writer().with(SerializationFeature.INDENT_OUTPUT)
				.without(SerializationFeature.WRITE_NULL_MAP_VALUES);
	}

	private static GeoApiContext createContext() throws IOException {
		return new GeoApiContext().setApiKey(new String(Files.readAllBytes(Paths.get("api-key")), "UTF-8"));
	}
}
