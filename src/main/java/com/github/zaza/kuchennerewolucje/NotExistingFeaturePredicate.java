package com.github.zaza.kuchennerewolucje;

import java.util.function.Predicate;

import org.geojson.FeatureCollection;

import com.github.zaza.kuchennerewolucje.model.Episode;

class NotExistingFeaturePredicate implements Predicate<Episode> {

	private FeatureCollection existingFeatures;

	NotExistingFeaturePredicate(FeatureCollection existingFeatures) {
		this.existingFeatures = existingFeatures;
	}

	@Override
	public boolean test(Episode episode) {
		return existingFeatures.getFeatures().stream()
				.noneMatch(f -> Integer.valueOf(episode.getNumber()).equals(f.getProperty("episode"))
						&& Integer.valueOf(episode.getSeason()).equals(f.getProperty("season")) );
	}

}
