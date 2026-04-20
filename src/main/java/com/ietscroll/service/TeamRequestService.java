package com.ietscroll.service;

import java.util.List;
import java.util.UUID;

import com.ietscroll.general.enums.TeamRequestStatus;
import com.ietscroll.response.ApplicationResponse;
import com.ietscroll.response.Result;
import com.ietscroll.response.TeamJoinResponse;

public interface TeamRequestService {
	
	Result requestToJoinTeam(String joinerEmail, UUID teamId,  String msg);

	List<ApplicationResponse> getMyApplications(String joinerEmail);
	List<TeamJoinResponse> getTeamRequests(String ownerEmail,TeamRequestStatus teamRequestStatus);
	
	Result acceptMember(String ownerEmail, String joinerEmail);

	Result rejectMember(String ownerEmail, String joinerEmail);

	Result kickMember(String ownerEmail, String memberEmail);
}
