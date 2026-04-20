package com.ietscroll.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ietscroll.general.enums.TeamRequestStatus;
import com.ietscroll.request.TeamJoiningRequest;
import com.ietscroll.response.ApplicationResponse;
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

		return teamRequestService.getTeamRequests(authentication.getName(), TeamRequestStatus.WAIT);
	}

	@GetMapping("/team-members")
	public List<TeamJoinResponse> getTeamMember(Authentication authentication) {

		return teamRequestService.getTeamRequests(authentication.getName(), TeamRequestStatus.ACCEPTED);
	}

	@PatchMapping("/accept/{applicantEmail}")
	public Result acceptRequest(Authentication authentication, @PathVariable String applicantEmail) {
		return teamRequestService.acceptMember(authentication.getName(), applicantEmail);
	}

	@PatchMapping("/reject/{applicantEmail}")
	public Result rejectRequest(Authentication authentication, @PathVariable String applicantEmail) {
		return teamRequestService.rejectMember(authentication.getName(), applicantEmail);
	}

	@PatchMapping("/remove/{applicantEmail}")
	public Result removeMember(Authentication authentication, @PathVariable String applicantEmail) {
		return teamRequestService.kickMember(authentication.getName(), applicantEmail);
	}

	@GetMapping("/my-application")
	public List<ApplicationResponse> getMyApplication(Authentication authentication) {
		return teamRequestService.getMyApplications(authentication.getName());
	}

}
