package com.github.zaza.kuchennerewolucje;

import static java.lang.String.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.zaza.kuchennerewolucje.model.Episode;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class DataSetChecker {
	public static void main(String[] args) throws IOException {
		Multimap<Integer, Episode> episodesBySeason = EpisodesReader.readAllEpisodes();
		Collection<Episode> episodes = episodesBySeason.values();
		System.out.println("odcinki :\t\t" + episodes.size());

		long open = episodes.stream().filter(e -> e.isOpen()).count();
		System.out.println("zamknięte na stałe :\t" + (episodes.size() - open));

		ArrayList<Integer> seasons = Lists.newArrayList(episodesBySeason.keySet().iterator());
		Collections.sort(seasons);
		Integer lastSeason = Iterables.getLast(seasons);

		System.out.println("ostatni sezon :\t\t" + lastSeason);

		List<Integer> missingSeasons = findMissingNumbers(seasons, lastSeason);
		System.out.println(format("brakujące sezony :\t%s", missingSeasons.isEmpty() ? "brak" : missingSeasons));

		for (Integer season : episodesBySeason.keySet()) {
			List<Episode> episodeList = new ArrayList<>(episodesBySeason.get(season));
			Collections.sort(episodeList, new Comparator<Episode>() {

				@Override
				public int compare(Episode e1, Episode e2) {
					return e1.getNumber() - e2.getNumber();
				}
			});
			Integer lastEpisode = Iterables.getLast(episodeList).getNumber();
			List<Integer> episodeNumbers = episodeList.stream().map(e -> e.getNumber()).collect(Collectors.toList());
			List<Integer> missingEpisodes = findMissingNumbers(episodeNumbers, lastEpisode);

			if (!missingEpisodes.isEmpty())
				System.out.println(format("brakujące odcinki dla sezonu %s: %s", season, missingEpisodes));
		}
	}

	private static List<Integer> findMissingNumbers(Collection<Integer> numbers, int max) {
		List<Integer> missingNumbers = new ArrayList<>();
		for (int i = 1; i < max + 1; i++) {
			if (!numbers.contains(i))
				missingNumbers.add(i);
		}
		return missingNumbers;
	}
}
