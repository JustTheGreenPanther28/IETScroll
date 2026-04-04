package com.ietscroll.util;

import org.springframework.stereotype.Component;

import com.ietscroll.dto.SightEngineResponse;
import com.ietscroll.exception.InappropriateImageException;

@Component
public class ModerationEvaluator {

	public void evaluate(SightEngineResponse r) {

		if (r.getNudity() != null) {
			var n = r.getNudity();
			if (n.getSexual_activity() > 0.5)
				throw new InappropriateImageException("sexual_activity");
			if (n.getSexual_display() > 0.5)
				throw new InappropriateImageException("sexual_display");
			if (n.getErotica() > 0.6)
				throw new InappropriateImageException("erotica");
			if (n.getVery_suggestive() > 0.7)
				throw new InappropriateImageException("very_suggestive");
		}

		if (r.getGore() != null)
			if (r.getGore().getProb() > 0.5)
				throw new InappropriateImageException("gore");

		if (r.getViolence() != null)
			if (r.getViolence().getProb() > 0.6)
				throw new InappropriateImageException("violence");

		if (r.getSelf_harm() != null)
			if (r.getSelf_harm().getProb() > 0.5)
				throw new InappropriateImageException("self_harm");

		if (r.getAlcohol() != null)
			if (r.getAlcohol().getProb() > 0.8)
				throw new InappropriateImageException("alcohol");

		if (r.getRecreational_drug() != null)
			if (r.getRecreational_drug().getProb() > 0.6)
				throw new InappropriateImageException("drug");

		if (r.getOffensive() != null) {
			var o = r.getOffensive();
			if (o.getNazi() > 0.5)
				throw new InappropriateImageException("nazi");
			if (o.getTerrorist() > 0.5)
				throw new InappropriateImageException("terrorist");
			if (o.getMiddle_finger() > 0.8)
				throw new InappropriateImageException("middle_finger");
		}

		if (r.getWeapon() != null && r.getWeapon().getClasses() != null) {
			var classes = r.getWeapon().getClasses();
			if (classes.getOrDefault("firearm", 0.0) > 0.6)
				throw new InappropriateImageException("firearm");
			if (classes.getOrDefault("knife", 0.0) > 0.7)
				throw new InappropriateImageException("knife");
		}
	}
}