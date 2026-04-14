package com.ietscroll.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.ietscroll.dto.TeamDTO;
import com.ietscroll.response.Result;
import com.ietscroll.response.TeamResponse;

public interface TeamService {
	TeamResponse createTeam(String ownerEmail, TeamDTO team);

	Page<TeamResponse> getActiveTeams(int page,int size);

	Result changeTeamSize(String ownerEmail, int teamSize);
	Result closeTeam(String ownerEmail);

	List<TeamResponse> getMyTeamPosts(String ownerEmail);

}
