package com.ietscroll.controller;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ietscroll.dto.TeamDTO;
import com.ietscroll.request.TeamCreationRequest;
import com.ietscroll.response.TeamResponse;
import com.ietscroll.service.TeamService;

@RestController
@RequestMapping("/api/v1/team")
public class TeamController {
	
	private TeamService teamService;
	private ModelMapper modelMapper;

	public TeamController(TeamService teamService ,ModelMapper modelMapper) {
		this.teamService = teamService;
		this.modelMapper=modelMapper;
	}
	
	@PostMapping
	public TeamResponse createTeam(Authentication authentication, @RequestBody TeamCreationRequest teamCreationRequest) {
		TeamDTO teamDTO = new TeamDTO();
		teamDTO.setPurpose(teamCreationRequest.purpose());
		teamDTO.setMaxMember(teamCreationRequest.teamSize());
		teamDTO.setSkillIds(teamCreationRequest.skillIds());
		
		return teamService.createTeam(authentication.getName(),teamDTO);
		
		
	}
	
}
