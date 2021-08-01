package com.github.zaza.kuchennerewolucje;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.geojson.FeatureCollection;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
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
				.filter(notExisting).map(new EpisodeToFeatureMapper(context)) //
				.filter(Optional::isPresent) //
				.forEach(f -> updatedFeatures.add(f.get()));
		return updatedFeatures;
	}

	private void write(FeatureCollection updatedFeatures) throws IOException {
		ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
		ObjectWriter writer = mapper.writer().with(SerializationFeature.INDENT_OUTPUT);
		writer.writeValue(GEOJSON_FILE, updatedFeatures);
	}

	private GeoApiContext createContext() throws IOException {
		Path apiKey = Paths.get("api-key");
		checkState(apiKey.toFile().exists(), "file with API key not found");
		return new GeoApiContext.Builder().apiKey(new String(Files.readAllBytes(apiKey), Charsets.UTF_8)).build();
	}
}
