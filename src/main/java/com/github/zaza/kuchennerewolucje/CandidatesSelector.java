package com.github.zaza.kuchennerewolucje;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.maps.model.PlacesSearchResult;

class CandidatesSelector {

	private static final Logger LOG = LoggerFactory.getLogger(CandidatesSelector.class);

	private final String query;

	CandidatesSelector(String query) {
		this.query = query;
	}

	public Optional<PlacesSearchResult> select(PlacesSearchResult[] candidates) {
		if (candidates.length == 0) {
			LOG.warn(format("No candidates found for '%s'", query));
			return Optional.empty();
		}
		if (candidates.length == 1)
			return Optional.of(candidates[0]);

		var byType = Arrays.stream(candidates).filter(withType("food", "restaurant"))
				.findFirst();
		if (byType.isPresent())
			return byType;

		var isOpen = Arrays.stream(candidates).filter(c -> !c.permanentlyClosed).findFirst();
		if (isOpen.isPresent())
			return isOpen;

		LOG.warn(format("Multiple matching candidates found for '%s'. Using the first one.", query));
		return Optional.of(candidates[0]);
	}

	private Predicate<PlacesSearchResult> withType(String... types) {
		return candidate -> {
			if (candidate.types == null || candidate.types.length == 0)
				return false;
			return Arrays.stream(candidate.types).anyMatch(t -> Arrays.asList(types).contains(t));
		};
	}

}
