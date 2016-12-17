package com.github.zaza.kuchennerewolucje;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Point;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

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
					.map(e -> convertToFeature(e)) //
					.filter(f -> f.isPresent()) //
					.forEach(f -> featureCollection.add(f.get()));
		}
		createObjectWriter().writeValue(new File("data.geojson"), featureCollection);
	}

	private static ObjectWriter createObjectWriter() {
		return new ObjectMapper().writer().with(SerializationFeature.INDENT_OUTPUT)
				.without(SerializationFeature.WRITE_NULL_MAP_VALUES);
	}

	private static Optional<Feature> convertToFeature(Map<String, Object> episode) {
		String address = (String) episode.get("adres");
		Optional<GeocodingResult> result = geocode(address);
		if (!result.isPresent())
			return Optional.empty();
		Feature feature = new Feature();
		Point point = new Point(result.get().geometry.location.lng, result.get().geometry.location.lat);
		feature.setGeometry(point);
		feature.getProperties().put("nazwa", episode.get("nazwa"));
		feature.getProperties().put("url", episode.get("url"));
		return Optional.of(feature);
	}

	private static Optional<GeocodingResult> geocode(String address) {
		try {
			GeocodingResult[] results = GeocodingApi.geocode(CONTEXT, address).await();
			if (results.length < 1) {
				System.out.println(format("No geocode results found for '%s'", address));
				return Optional.empty();
			}
			if (results.length > 1) {
				System.out.println(format("Found multiple geocode results for '%s'.", address));
			}
			return Optional.of(results[0]);
		} catch (Exception e) {
			System.err.println(format("Error occured retrieving coordinates for '%s': %s", address, e.getMessage()));
			return Optional.empty();
		}
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