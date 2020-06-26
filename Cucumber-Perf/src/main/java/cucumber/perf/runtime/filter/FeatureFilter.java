package cucumber.perf.runtime.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.internal.gherkin.events.PickleEvent;
//import io.cucumber.core.gherkin.messages.*;

public class FeatureFilter {
	public static Pattern TAG_PATTERN = Pattern.compile("((\\(?@\\w* (or|and) @\\w*\\)?( (or|and) @\\w*)?)|not @\\w*|@\\w*(?=[ ,])|@\\w*(?=[@])|@\\w*)");//"@(.*)(?=[ ,])|@.*(?=[@])|@.*");
	private List<Feature> features;

	public FeatureFilter(List<Feature> features) {
		this.features = features;
	}

	public List<Feature> filter(String text) {
		List<Feature> features = new ArrayList<Feature>();
		for (Feature feature : this.features) {
			Feature result =this.getMatch(feature, text);
			if (result != null) {
				features.add(feature);
			}
		}
		return features;
	}
	
	private Feature getMatch(Feature feature, String text) {
		if (text.startsWith("@")) {
			Feature result = feature;//new Feature(feature.children(), feature.getUri(), "", feature.getPickles());
			List<String> tags = getGroupTags(text);
			TagPredicate tp = new TagPredicate(tags);
			boolean isValid = false;
			for (int p = 0; p<result.getPickles().size();p++) {
				if (!tp.test(result.getPickles().get(p))) {
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
			if (feature.getName().equalsIgnoreCase(text) || feature.getUri().toString()
					.substring(feature.getUri().toString().lastIndexOf("/") + 1).equalsIgnoreCase(text))
				return feature;
		}
		return null;
	}
	
	public static boolean isMatch(Feature feature, String text) {
		if (text.startsWith("@")) {
			List<String> tags = getGroupTags(text);
			TagPredicate tp = new TagPredicate(tags);
			for (Pickle pe : feature.getPickles()) {
				if (tp.test(pe)) {
					return true;
				}
			}
		} else {
			if (feature.getName().equalsIgnoreCase(text) || feature.getUri().toString()
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