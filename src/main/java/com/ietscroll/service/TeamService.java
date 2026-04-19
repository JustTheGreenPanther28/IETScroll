package com.ietscroll.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.ietscroll.dto.TeamDTO;
import com.ietscroll.response.Result;
import com.ietscroll.response.TeamResponse;

public interface TeamService {
	TeamResponse createTeam(String ownerEmail, TeamDTO team);
	
	TeamResponse getMyTeamDetails(String onwerEmail);
	TeamResponse getTeamById(UUID publicId);
	Page<TeamResponse> getActiveTeams(int page,int size);

	boolean isTeamExist(UUID publicId);
	Result changeTeamSize(String ownerEmail, int teamSize);
	Result closeTeam(String ownerEmail);

	List<TeamResponse> getMyTeamPosts(String ownerEmail);
}
