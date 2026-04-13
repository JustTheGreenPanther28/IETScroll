package com.ietscroll.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "team_finder_skills")
public class TeamFinderSkill {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	// Many TeamFinderSkill rows → one TeamFinder
	@ManyToOne
	@JoinColumn(name = "team_finder_id", nullable = false)
	private Team team;

	// Many TeamFinderSkill rows → one Skill
	@ManyToOne
	@JoinColumn(name = "skill_id", nullable = false)
	private Skills skill;

	public int getId() {
		return id;
	}

	public Skills getSkill() {
		return skill;
	}

	public void setSkill(Skills skill) {
		this.skill = skill;
	}


	public Team getTeam() {
		return team;
	}


	public void setTeam(Team team) {
		this.team = team;
	}
}