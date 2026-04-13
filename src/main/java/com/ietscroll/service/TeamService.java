package com.ietscroll.service;

import java.util.List;
import java.util.UUID;

import com.ietscroll.dto.TeamDTO;
import com.ietscroll.dto.TeamJoinRequestDTO;
import com.ietscroll.response.Result;
import com.ietscroll.response.TeamJoinResponse;
import com.ietscroll.response.TeamResponse;

public interface TeamService {
	TeamResponse createTeam(String ownerEmail, TeamDTO team);

	List<TeamResponse> getActiveTeams();
	List<TeamResponse> getMyApplications(String joinerEmail);

	TeamResponse changeTeamSize(String ownerEmail, int teamSize);
	TeamResponse closeTeam(String ownerEmail);

	List<TeamResponse> getMyTeamPosts(String ownerEmail);

	TeamJoinResponse requestToJoinTeam(String joinerEmail, UUID teamId, TeamJoinRequestDTO dto);
	List<TeamJoinResponse> getTeamRequests(String ownerEmail);

	Result acceptMember(String ownerEmail, String joinerEmail);
	Result rejectMember(String ownerEmail, String joinerEmail);
	Result kickMember(String ownerEmail, String memberEmail);

}
