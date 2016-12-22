package com.github.zaza.kuchennerewolucje;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zaza.kuchennerewolucje.model.Episode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class EpisodesReader {

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
	};

	private static final Pattern PATTERN_SEASON = Pattern.compile("sezon(\\d{1,2})\\.json");

	static Multimap<Integer, Episode> readAllEpisodes() throws IOException {
		ArrayListMultimap<Integer, Episode> result = ArrayListMultimap.create();
		DirectoryStream<Path> seasons = listSeasons();
		for (Path season : seasons) {
			int seasonNumber = getSeasonNumber(season);
			List<Map<String, Object>> episodes = readEpisodes(season.toFile());
			for (Map<String, Object> episode : episodes) {
				result.put(seasonNumber, new Episode(episode));
			}
		}
		return result;
	}

	static DirectoryStream<Path> listSeasons() throws IOException {
		return Files.newDirectoryStream(Paths.get("data"), "sezon*.json");
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> readEpisodes(File season) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> sezon = (Map<String, Object>) mapper.readValue(season, MAP_TYPE);
		return (List<Map<String, Object>>) sezon.get("odcinki");
	}

	private static int getSeasonNumber(Path season) {
		Matcher matcher = PATTERN_SEASON.matcher(season.getFileName().toString());
		matcher.find();
		return Integer.parseInt(matcher.group(1));
	}
}
