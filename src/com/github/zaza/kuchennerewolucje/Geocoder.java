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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

public class Geocoder {

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
	};

	public static void main(String[] args) throws Exception {
		DirectoryStream<Path> seasons = listSeasons();
		FeatureCollection featureCollection = new FeatureCollection();
		for (Path season : seasons) {
			List<Map<String, Object>> episodes = readEpisodes(season.toFile());
			for (Map<String, Object> episode : episodes) {
				Optional<Feature> feature = convertToFeature(episode);
				if (feature.isPresent())
					featureCollection.add(feature.get());
			}
		}
		ObjectWriter writer = new ObjectMapper().writer().with(SerializationFeature.INDENT_OUTPUT)
				.without(SerializationFeature.WRITE_NULL_MAP_VALUES);
		writer.writeValue(new File("data.geojson"), featureCollection);
	}

	private static Optional<Feature> convertToFeature(Map<String, Object> episode) throws Exception {
		GeoApiContext context = createContext();
		String adres = (String) episode.get("adres");
		GeocodingResult[] results = GeocodingApi.geocode(context, adres).await();
		if (results.length < 1) {
			System.err.println(format("Nothing found for '%s'", adres));
			return Optional.empty();
		}
		Feature feature = new Feature();
		Point point = new Point(results[0].geometry.location.lng, results[0].geometry.location.lat);
		feature.setGeometry(point);
		feature.getProperties().put("nazwa", episode.get("nazwa"));
		feature.getProperties().put("url", episode.get("url"));
		return Optional.of(feature);
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> readEpisodes(File season)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> sezon = (Map<String, Object>) mapper.readValue(season, MAP_TYPE);
		return (List<Map<String, Object>>) sezon.get("odcinki");
	}

	private static DirectoryStream<Path> listSeasons() throws IOException {
		return Files.newDirectoryStream(new File("data").toPath(), "sezon*.json");
	}

	private static GeoApiContext createContext() throws IOException {
		return new GeoApiContext().setApiKey(new String(Files.readAllBytes(Paths.get("api-key")), "UTF-8"));
	}
}
