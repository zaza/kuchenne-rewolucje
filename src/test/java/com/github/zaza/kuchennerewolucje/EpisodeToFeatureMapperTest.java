package com.github.zaza.kuchennerewolucje;

import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import org.geojson.Feature;
import org.junit.Test;

import com.github.zaza.kuchennerewolucje.model.Episode;

public class EpisodeToFeatureMapperTest {
	@Test
	public void applyFeatureNotCreatedForEpisodeWithoutName() throws Exception {
		EpisodeToFeatureMapper mapper = new EpisodeToFeatureMapper(null);

		Optional<Feature> feature = mapper.apply(new Episode(new HashMap<>()));

		assertFalse(feature.isPresent());
	}

	@Test
	public void applyFeatureNotCreatedForEpisodeWithEmptyName() throws Exception {
		EpisodeToFeatureMapper mapper = new EpisodeToFeatureMapper(null);

		Optional<Feature> feature = mapper.apply(newEpisode("nazwa", ""));

		assertFalse(feature.isPresent());
	}

	private static Episode newEpisode(String key, Object value) {
		return new Episode(Collections.singletonMap(key, value));
	}
}
