package com.github.zaza.kuchennerewolucje;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.geojson.FeatureCollection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.maps.GeoApiContext;

public class Geocoder {

	private static final File GEOJSON_FILE = new File("data.geojson");
	
	private GeoApiContext context;

	public static void main(String[] args) throws Exception {
		Geocoder geocoder = new Geocoder();
		FeatureCollection existingFeatures = FeatureCollectionReader.read(GEOJSON_FILE);
		FeatureCollection updatedFeatures = geocoder.mapExistingEpisodesToFeatures(existingFeatures);
		geocoder.write(updatedFeatures);
	}

	private Geocoder() throws IOException {
		context = createContext();
	}

	private FeatureCollection mapExistingEpisodesToFeatures(FeatureCollection existingFeatures) throws IOException {
		FeatureCollection updatedFeatures = new FeatureCollection();
		updatedFeatures.setFeatures(existingFeatures.getFeatures());
		NotExistingFeaturePredicate notExisting = new NotExistingFeaturePredicate(updatedFeatures); 
		EpisodesReader.readAllEpisodes().values().stream() //
				.filter(notExisting)
				.map(new EpisodeToFeatureMapper(context)) //
				.filter(f -> f.isPresent()) //
				.forEach(f -> updatedFeatures.add(f.get()));
		return updatedFeatures;
	}

	private void write(FeatureCollection updatedFeatures) throws IOException {
		ObjectWriter writer = new ObjectMapper().writer().with(SerializationFeature.INDENT_OUTPUT)
		.without(SerializationFeature.WRITE_NULL_MAP_VALUES);
		writer.writeValue(GEOJSON_FILE, updatedFeatures);
	}

	private GeoApiContext createContext() throws IOException {
		Path apiKey = Paths.get("api-key");
		checkState(apiKey.toFile().exists(), "file with API key not found");
		return new GeoApiContext.Builder().apiKey(new String(Files.readAllBytes(apiKey), "UTF-8")).build();
	}
}
