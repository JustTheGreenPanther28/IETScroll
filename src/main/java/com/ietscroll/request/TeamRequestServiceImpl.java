package com.ietscroll.request;

import java.util.List;
import java.util.UUID;

import com.ietscroll.dto.TeamJoinRequestDTO;
import com.ietscroll.response.Result;
import com.ietscroll.response.TeamJoinResponse;
import com.ietscroll.response.TeamResponse;
import com.ietscroll.service.TeamRequestService;

public class TeamRequestServiceImpl implements TeamRequestService{
	@Override
	public TeamJoinResponse requestToJoinTeam(String joinerEmail, UUID teamId, TeamJoinRequestDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<TeamJoinResponse> getTeamRequests(String ownerEmail) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result acceptMember(String ownerEmail, String joinerEmail) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result rejectMember(String ownerEmail, String joinerEmail) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result kickMember(String ownerEmail, String memberEmail) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TeamResponse> getMyApplications(String joinerEmail) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TeamJoinResponse> getTeamMMember(String ownerEmail) {
		// TODO Auto-generated method stub
		return null;
	}
}
