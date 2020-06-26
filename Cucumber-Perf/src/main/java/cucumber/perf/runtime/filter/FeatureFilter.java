package cucumber.perf.runtime.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cucumber.runtime.filter.TagPredicate;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;

public class FeatureFilter {
	public static Pattern TAG_PATTERN = Pattern.compile("((\\(?@\\w* (or|and) @\\w*\\)?( (or|and) @\\w*)?)|not @\\w*|@\\w*(?=[ ,])|@\\w*(?=[@])|@\\w*)");//"@(.*)(?=[ ,])|@.*(?=[@])|@.*");
	private List<CucumberFeature> features;

	public FeatureFilter(List<CucumberFeature> features) {
		this.features = features;
	}

	public List<CucumberFeature> filter(String text) {
		List<CucumberFeature> features = new ArrayList<CucumberFeature>();
		for (CucumberFeature feature : this.features) {
			CucumberFeature result =this.getMatch(feature, text);
			if (result != null) {
				features.add(feature);
			}
		}
		return features;
	}
	
	private CucumberFeature getMatch(CucumberFeature feature, String text) {
		if (text.startsWith("@")) {
			CucumberFeature result = new CucumberFeature(feature.getGherkinFeature(), feature.getUri(), "", feature.getPickles());
			List<String> tags = getGroupTags(text);
			TagPredicate tp = new TagPredicate(tags);
			boolean isValid = false;
			for (int p = 0; p<result.getPickles().size();p++) {
				if (!tp.apply(result.getPickles().get(p))) {
					result.getPickles().remove(p);
				} else {
					isValid = true;
				}
			}
			if (isValid)
			{
				return result;
			}
		} else {
			if (feature.getGherkinFeature().getFeature().getName().equalsIgnoreCase(text) || feature.getUri().toString()
					.substring(feature.getUri().toString().lastIndexOf("/") + 1).equalsIgnoreCase(text))
				return feature;
		}
		return null;
	}
	
	public static boolean isMatch(CucumberFeature feature, String text) {
		if (text.startsWith("@")) {
			List<String> tags = getGroupTags(text);
			TagPredicate tp = new TagPredicate(tags);
			for (PickleEvent pe : feature.getPickles()) {
				if (tp.apply(pe)) {
					return true;
				}
			}
		} else {
			if (feature.getGherkinFeature().getFeature().getName().equalsIgnoreCase(text) || feature.getUri().toString()
					.substring(feature.getUri().toString().lastIndexOf("/") + 1).equalsIgnoreCase(text))
				return true;
		}
		return false;
	}
	
	public static List<String> getGroupTags(String text) {
		List<String> tags = new ArrayList<String>();
		if (text.startsWith("@")) {
			Matcher m = TAG_PATTERN.matcher(text);
			while (m.find()) {
				tags.add(m.group());
			}
		}
		return tags;
	}
}