package com.github.zaza.kuchennerewolucje;

import static java.lang.String.format;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geojson.Feature;
import org.geojson.Point;

import com.github.zaza.kuchennerewolucje.model.Episode;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlacesApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResult;

class EpisodeToFeatureMapper implements Function<Episode, Optional<Feature>> {

	private static final String MARKER_RED = "https://raw.githubusercontent.com/Concept211/Google-Maps-Markers/master/images/marker_red.png";

	private static final String MARKER_PURPLE = "https://raw.githubusercontent.com/Concept211/Google-Maps-Markers/master/images/marker_purple.png";

	private static final Pattern PATTERN_ZIPCODE = Pattern.compile("\\d{2}-\\d{3} ([\\w ]+)",
			Pattern.UNICODE_CHARACTER_CLASS);

	private GeoApiContext context;

	EpisodeToFeatureMapper(GeoApiContext context) {
		this.context = context;
	}

	@Override
	public Optional<Feature> apply(Episode episode) {
		String address = episode.getAddress();
		String name = episode.getName();
		if (hasZipCode(address)) {
			Optional<GeocodingResult> result = geocode(address);
			if (result.isPresent()) {
				Feature feature = new Feature();
				Point point = new Point(result.get().geometry.location.lng, result.get().geometry.location.lat);
				feature.setGeometry(point);
				feature.getProperties().put("name", name);
				feature.getProperties().put("url", episode.getUrl());
				if (episode.hasHomepage())
					feature.getProperties().put("homepage", episode.getHomepage());
				feature.getProperties().put("icon", getIcon(episode));
				return Optional.of(feature);
			}
		}
		String city = getCity(address);
		String query = format("%s, %s", name, city);
		Optional<PlacesSearchResult> result = textSearchQuery(query);
		if (!result.isPresent())
			return Optional.empty();
		Feature feature = new Feature();
		Point point = new Point(result.get().geometry.location.lng, result.get().geometry.location.lat);
		feature.setGeometry(point);
		feature.getProperties().put("name", name);
		feature.getProperties().put("url", episode.getUrl());
		if (episode.hasHomepage())
			feature.getProperties().put("homepage", episode.getHomepage());
		// TODO: OpeningHours.permanentlyClosed
		feature.getProperties().put("icon", getIcon(episode));
		return Optional.of(feature);
	}

	private Optional<GeocodingResult> geocode(String address) {
		try {
			GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
			return getFirst(results, address);
		} catch (Exception e) {
			System.err.println(format("Error occured retrieving geocode for '%s': %s", address, e.getMessage()));
			return Optional.empty();
		}
	}

	private Optional<PlacesSearchResult> textSearchQuery(String query) {
		try {
			PlacesSearchResult[] results = PlacesApi.textSearchQuery(context, query).type(PlaceType.FOOD)
					.type(PlaceType.RESTAURANT).await().results;
			return getFirst(results, query);
		} catch (Exception e) {
			System.err.println(format("Error occured retrieving places for '%s': %s", query, e.getMessage()));
			return Optional.empty();
		}
	}

	private static <T> Optional<T> getFirst(T[] array, String query) {
		if (array.length < 1) {
			System.out.println(format("No results found for '%s'", query));
			return Optional.empty();
		}
		if (array.length > 1) {
			System.out.println(format("Multiple results found for '%s'. Using the first one.", query));
		}
		return Optional.of(array[0]);
	}

	private static String getIcon(Episode episode) {
		return episode.isOpen() ? MARKER_RED : MARKER_PURPLE;
	}

	static boolean hasZipCode(String address) {
		return PATTERN_ZIPCODE.matcher(address).find();
	}

	static String getCity(String address) {
		Matcher matcher = PATTERN_ZIPCODE.matcher(address);
		return matcher.find() ? matcher.group(1) : address;
	}

}
