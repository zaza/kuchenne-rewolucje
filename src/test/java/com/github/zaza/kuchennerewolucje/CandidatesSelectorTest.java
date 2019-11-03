package com.github.zaza.kuchennerewolucje;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;

import com.google.maps.model.PlacesSearchResult;

public class CandidatesSelectorTest {
	private final CandidatesSelector selector = new CandidatesSelector("foo");

	@Test
	public void selectNoCandidates() throws Exception {
		Optional<PlacesSearchResult> selected = selector.select(new PlacesSearchResult[]{});
		assertThat(selected).isEmpty();
	}

	@Test
	public void selectSingle() throws Exception {
		PlacesSearchResult result = new PlacesSearchResult();

		Optional<PlacesSearchResult> selected = selector.select(new PlacesSearchResult[]{result});

		assertThat(selected).isPresent().contains(result);
	}

	@Test
	public void selecTwoIdenticalCandidates() throws Exception {
		PlacesSearchResult result1 = new PlacesSearchResult();
		PlacesSearchResult result2 = new PlacesSearchResult();

		Optional<PlacesSearchResult> selected = selector.select(new PlacesSearchResult[]{result1, result2});

		assertThat(selected).isPresent().contains(result1);
	}
	
	@Test
	public void selecAllClosed() throws Exception {
		PlacesSearchResult result1 = new PlacesSearchResult();
		result1.permanentlyClosed = true;
		PlacesSearchResult result2 = new PlacesSearchResult();
		result2.permanentlyClosed = true;

		Optional<PlacesSearchResult> selected = selector.select(new PlacesSearchResult[]{result1, result2});

		assertThat(selected).isPresent().contains(result1);
	}

	@Test
	public void selecFirstNotPermanentlyClosed() throws Exception {
		PlacesSearchResult result1 = new PlacesSearchResult();
		result1.permanentlyClosed = true;
		PlacesSearchResult result2 = new PlacesSearchResult();
		PlacesSearchResult result3 = new PlacesSearchResult();

		Optional<PlacesSearchResult> selected = selector.select(new PlacesSearchResult[]{result1, result2, result3});

		assertThat(selected).isPresent().contains(result2);
	}

	@Test
	public void selectByType() throws Exception {
		PlacesSearchResult result1 = new PlacesSearchResult();
		PlacesSearchResult result2 = new PlacesSearchResult();
		result2.types = new String[]{"school"};
		PlacesSearchResult result3 = new PlacesSearchResult();
		result3.types = new String[]{"food"};

		Optional<PlacesSearchResult> selected = selector.select(new PlacesSearchResult[]{result1, result2, result3});

		assertThat(selected).isPresent().contains(result3);
	}
	
	@Test
	public void selectByTypeEvenIfPermanentlyClosed() throws Exception {
		PlacesSearchResult result1 = new PlacesSearchResult();
		PlacesSearchResult result2 = new PlacesSearchResult();
		PlacesSearchResult result3 = new PlacesSearchResult();
		result3.types = new String[]{"food"};
		result1.permanentlyClosed = true;

		Optional<PlacesSearchResult> selected = selector.select(new PlacesSearchResult[]{result1, result2, result3});

		assertThat(selected).isPresent().contains(result3);
	}
}
