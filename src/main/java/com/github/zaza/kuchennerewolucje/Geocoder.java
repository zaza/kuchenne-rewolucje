package com.github.zaza.kuchennerewolucje;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.geojson.FeatureCollection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.zaza.kuchennerewolucje.model.Episode;
import com.google.maps.GeoApiContext;

public class Geocoder {

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
	};

	private static GeoApiContext CONTEXT;

	public static void main(String[] args) throws Exception {
		CONTEXT = createContext();
		DirectoryStream<Path> seasons = listSeasons();
		FeatureCollection featureCollection = new FeatureCollection();
		for (Path season : seasons) {
			readEpisodes(season.toFile()).stream() //
					.map(e -> new Episode(e))
					.map(new EpisodeToFeatureMapper(CONTEXT)) //
					.filter(f -> f.isPresent()) //
					.forEach(f -> featureCollection.add(f.get()));
		}
		createObjectWriter().writeValue(new File("data.geojson"), featureCollection);
	}

	private static ObjectWriter createObjectWriter() {
		return new ObjectMapper().writer().with(SerializationFeature.INDENT_OUTPUT)
				.without(SerializationFeature.WRITE_NULL_MAP_VALUES);
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> readEpisodes(File season) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> sezon = (Map<String, Object>) mapper.readValue(season, MAP_TYPE);
		return (List<Map<String, Object>>) sezon.get("odcinki");
	}

	private static DirectoryStream<Path> listSeasons() throws IOException {
		return Files.newDirectoryStream(Paths.get("data"), "sezon*.json");
	}

	private static GeoApiContext createContext() throws IOException {
		return new GeoApiContext().setApiKey(new String(Files.readAllBytes(Paths.get("api-key")), "UTF-8"));
	}
}
