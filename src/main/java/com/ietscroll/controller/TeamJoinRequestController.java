package com.ietscroll.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ietscroll.general.enums.TeamRequestStatus;
import com.ietscroll.request.TeamJoiningRequest;
import com.ietscroll.response.Result;
import com.ietscroll.response.TeamJoinResponse;
import com.ietscroll.service.TeamRequestService;

@RestController
@RequestMapping("/api/v1/request-team")
public class TeamJoinRequestController {

	private final TeamRequestService teamRequestService;

	public TeamJoinRequestController(TeamRequestService teamRequestService) {
		this.teamRequestService = teamRequestService;
	}

	@PostMapping
	public Result requestToJoin(Authentication authentication, @RequestBody TeamJoiningRequest teamJoiningRequest) {

		return teamRequestService.requestToJoinTeam(authentication.getName(),
				UUID.fromString(teamJoiningRequest.teamId()), teamJoiningRequest.message());
	}
	
	@GetMapping("/requests")
	public List<TeamJoinResponse> getRequests(Authentication authentication) {
		
		return teamRequestService.getTeamRequests(authentication.getName(),TeamRequestStatus.WAIT);
	}
	
	@GetMapping("/team-members")
	public List<TeamJoinResponse> getTeamMember(Authentication authentication){
		
		return teamRequestService.getTeamRequests(authentication.getName(), TeamRequestStatus.ACCEPTED);
	}
	
	

}
