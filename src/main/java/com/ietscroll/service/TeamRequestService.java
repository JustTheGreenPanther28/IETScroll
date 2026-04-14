package com.ietscroll.service;

import java.util.List;
import java.util.UUID;

import com.ietscroll.dto.TeamJoinRequestDTO;
import com.ietscroll.response.Result;
import com.ietscroll.response.TeamJoinResponse;
import com.ietscroll.response.TeamResponse;

public interface TeamRequestService {
	
	TeamJoinResponse requestToJoinTeam(String joinerEmail, UUID teamId, TeamJoinRequestDTO dto);

	List<TeamResponse> getMyApplications(String joinerEmail);

	List<TeamJoinResponse> getTeamRequests(String ownerEmail);

	Result acceptMember(String ownerEmail, String joinerEmail);

	Result rejectMember(String ownerEmail, String joinerEmail);

	Result kickMember(String ownerEmail, String memberEmail);
}
