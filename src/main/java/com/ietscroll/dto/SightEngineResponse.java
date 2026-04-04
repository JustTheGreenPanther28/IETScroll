package com.ietscroll.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SightEngineResponse {

	private String status;
	private Nudity nudity;
	private Weapon weapon;
	private Gore gore;
	private Violence violence;
	private SimpleProb alcohol;
	private SimpleProb recreational_drug;
	private Offensive offensive;
	private SimpleProb self_harm;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Nudity getNudity() {
		return nudity;
	}

	public void setNudity(Nudity nudity) {
		this.nudity = nudity;
	}

	public Weapon getWeapon() {
		return weapon;
	}

	public void setWeapon(Weapon weapon) {
		this.weapon = weapon;
	}

	public Gore getGore() {
		return gore;
	}

	public void setGore(Gore gore) {
		this.gore = gore;
	}

	public Violence getViolence() {
		return violence;
	}

	public void setViolence(Violence violence) {
		this.violence = violence;
	}

	public SimpleProb getAlcohol() {
		return alcohol;
	}

	public void setAlcohol(SimpleProb alcohol) {
		this.alcohol = alcohol;
	}

	public SimpleProb getRecreational_drug() {
		return recreational_drug;
	}

	public void setRecreational_drug(SimpleProb recreational_drug) {
		this.recreational_drug = recreational_drug;
	}

	public Offensive getOffensive() {
		return offensive;
	}

	public void setOffensive(Offensive offensive) {
		this.offensive = offensive;
	}

	public SimpleProb getSelf_harm() {
		return self_harm;
	}

	public void setSelf_harm(SimpleProb self_harm) {
		this.self_harm = self_harm;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Nudity {
		private double sexual_activity;
		private double sexual_display;
		private double erotica;
		private double very_suggestive;

		public double getSexual_activity() {
			return sexual_activity;
		}

		public void setSexual_activity(double sexual_activity) {
			this.sexual_activity = sexual_activity;
		}

		public double getSexual_display() {
			return sexual_display;
		}

		public void setSexual_display(double sexual_display) {
			this.sexual_display = sexual_display;
		}

		public double getErotica() {
			return erotica;
		}

		public void setErotica(double erotica) {
			this.erotica = erotica;
		}

		public double getVery_suggestive() {
			return very_suggestive;
		}

		public void setVery_suggestive(double very_suggestive) {
			this.very_suggestive = very_suggestive;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Weapon {
		private Map<String, Double> classes;

		public Map<String, Double> getClasses() {
			return classes;
		}

		public void setClasses(Map<String, Double> classes) {
			this.classes = classes;
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Gore {
		private double prob;

		public double getProb() {
			return prob;
		}

		public void setProb(double prob) {
			this.prob = prob;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Violence {
		private double prob;

		public double getProb() {
			return prob;
		}

		public void setProb(double prob) {
			this.prob = prob;
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Offensive {
		private double nazi;
		private double terrorist;
		private double middle_finger;

		public double getNazi() {
			return nazi;
		}

		public void setNazi(double nazi) {
			this.nazi = nazi;
		}

		public double getTerrorist() {
			return terrorist;
		}

		public void setTerrorist(double terrorist) {
			this.terrorist = terrorist;
		}

		public double getMiddle_finger() {
			return middle_finger;
		}

		public void setMiddle_finger(double middle_finger) {
			this.middle_finger = middle_finger;
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SimpleProb {
		private double prob;

		public double getProb() {
			return prob;
		}

		public void setProb(double prob) {
			this.prob = prob;
		}

	}
}