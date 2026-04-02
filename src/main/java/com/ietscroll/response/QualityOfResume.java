package com.ietscroll.response;

import java.util.List;

public class QualityOfResume {

	private float overAllRating;
	private List<String> topFiveSkillsMatched;
	private List<String> topTenKeywordsMissed;
	private float rateContentQuality;
	private float rateProjects;
	private String improvement;
	private String suggestedUnqiueProject;

	public float getOverAllRating() {
		return overAllRating;
	}

	public void setOverAllRating(float overAllRating) {
		this.overAllRating = overAllRating;
	}

	public List<String> getTopFiveSkillsMatched() {
		return topFiveSkillsMatched;
	}

	public void setTopFiveSkillsMatched(List<String> topFiveSkillsMatched) {
		this.topFiveSkillsMatched = topFiveSkillsMatched;
	}

	public List<String> getTopTenKeywordsMissed() {
		return topTenKeywordsMissed;
	}

	public void setTopTenKeywordsMissed(List<String> topTenKeywordsMissed) {
		this.topTenKeywordsMissed = topTenKeywordsMissed;
	}

	public float getRateContentQuality() {
		return rateContentQuality;
	}

	public void setRateContentQuality(float rateContentQuality) {
		this.rateContentQuality = rateContentQuality;
	}

	public float getRateProjects() {
		return rateProjects;
	}

	public void setRateProjects(float rateProjects) {
		this.rateProjects = rateProjects;
	}

	public String getImprovement() {
		return improvement;
	}

	public void setImprovement(String improvement) {
		this.improvement = improvement;
	}

	public String getSuggestedUnqiueProject() {
		return suggestedUnqiueProject;
	}

	public void setSuggestedUnqiueProject(String suggestedUnqiueProject) {
		this.suggestedUnqiueProject = suggestedUnqiueProject;
	}

}
