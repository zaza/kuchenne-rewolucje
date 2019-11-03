package com.github.zaza.kuchennerewolucje;

import static java.lang.String.format;

import java.util.Optional;
import java.util.function.Function;

import org.geojson.Feature;
import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zaza.kuchennerewolucje.model.Episode;
import com.google.maps.FindPlaceFromTextRequest.FieldMask;
import com.google.maps.FindPlaceFromTextRequest.InputType;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlacesApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;

class EpisodeToFeatureMapper implements Function<Episode, Optional<Feature>> {

	private static final Logger LOG = LoggerFactory.getLogger(EpisodeToFeatureMapper.class);

	private GeoApiContext context;

	EpisodeToFeatureMapper(GeoApiContext context) {
		this.context = context;
	}

	@Override
	public Optional<Feature> apply(Episode episode) {
		String name = episode.getName();
		if (name == null || name.isEmpty())
			return Optional.empty();
		if (episode.hasZipCode()) {
			Optional<GeocodingResult> result = geocode(episode.getAddress());
			if (result.isPresent()) {
				return newFeature(episode, result.get());
			}
		}
		String city = episode.getCity();
		String query = format("%s, %s", name, city);
		Optional<PlacesSearchResult> result = textSearchQuery(query);
		if (!result.isPresent())
			return Optional.empty();
		return newFeature(episode, result.get());
	}

	private Optional<Feature> newFeature(Episode episode, GeocodingResult result) {
		Feature feature = new Feature();
		Point point = new Point(result.geometry.location.lng, result.geometry.location.lat);
		feature.setGeometry(point);
		feature.getProperties().put("name", episode.getName());
		feature.getProperties().put("url", episode.getUrl());
		addHomepageIfExists(feature, episode);
		feature.getProperties().put("icon", episode.getIcon());
		addSeasonAndEpisodeNumber(feature, episode);
		return Optional.of(feature);
	}

	private Optional<Feature> newFeature(Episode episode, PlacesSearchResult result) {
		Feature feature = new Feature();
		Point point = new Point(result.geometry.location.lng, result.geometry.location.lat);
		feature.setGeometry(point);
		// TODO: use the new name from the result
		feature.getProperties().put("name", episode.getName());
		feature.getProperties().put("url", episode.getUrl());
		addHomepageIfExists(feature, episode, result.placeId);
		feature.getProperties().put("icon", episode.getIcon(!result.permanentlyClosed));
		addSeasonAndEpisodeNumber(feature, episode);
		return Optional.of(feature);
	}

	private void addHomepageIfExists(Feature feature, Episode episode) {
		if (episode.hasHomepage())
			feature.getProperties().put("homepage", episode.getHomepage());
	}
	
	private void addHomepageIfExists(Feature feature, Episode episode, String placeId) {
		addHomepageIfExists(feature, episode);
		if (!feature.getProperties().containsKey("homepage")) {
			Optional<PlaceDetails> placeDetails = placeDetails(placeId);
			if (placeDetails.isPresent() && placeDetails.get().website != null) {
				feature.getProperties().put("homepage", placeDetails.get().website.toString());
			}
		}
	}
	
	private void addSeasonAndEpisodeNumber(Feature feature, Episode episode) {
		feature.getProperties().put("season", episode.getSeason());
		feature.getProperties().put("episode", episode.getNumber());
	}

	private Optional<GeocodingResult> geocode(String address) {
		try {
			GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
			return getFirst(results, address);
		} catch (Exception e) {
			LOG.error(format("Error occured retrieving geocode for '%s': %s", address, e.getMessage()));
			return Optional.empty();
		}
	}

	private Optional<PlacesSearchResult> textSearchQuery(String query) {
		try {
			PlacesSearchResult[] candidates = PlacesApi.findPlaceFromText(context, query, InputType.TEXT_QUERY)
					.fields(FieldMask.PLACE_ID, FieldMask.NAME, FieldMask.GEOMETRY, FieldMask.PERMANENTLY_CLOSED, FieldMask.TYPES).await().candidates;
			return new CandidatesSelector(query).select(candidates);
		} catch (Exception e) {
			LOG.error(format("Error occured retrieving places for '%s': %s", query, e.getMessage()));
			return Optional.empty();
		}
	}
	
	private Optional<PlaceDetails> placeDetails(String placeId) {
		try {
			PlaceDetails placeDetails = PlacesApi.placeDetails(context, placeId).await();
			return Optional.of(placeDetails);
		} catch (Exception e) {
			LOG.error(format("Error occured retrieving place details for '%s': %s", placeId, e.getMessage()));
			return Optional.empty();
		}
	}

	private static <T> Optional<T> getFirst(T[] array, String query) {
		if (array.length < 1) {
			LOG.warn(format("No results found for '%s'", query));
			return Optional.empty();
		}
		if (array.length > 1) {
			LOG.warn(format("Multiple results found for '%s'. Using the first one.", query));
		}
		return Optional.of(array[0]);
	}

}
