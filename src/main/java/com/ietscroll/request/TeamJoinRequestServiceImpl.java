package com.ietscroll.request;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ietscroll.dto.TeamJoinRequestDTO;
import com.ietscroll.entity.Team;
import com.ietscroll.entity.TeamJoinRequest;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.general.enums.TeamStatus;
import com.ietscroll.repository.TeamJoinRequestRepository;
import com.ietscroll.repository.TeamRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.Result;
import com.ietscroll.response.TeamJoinResponse;
import com.ietscroll.response.TeamResponse;
import com.ietscroll.service.TeamRequestService;
import com.ietscroll.service.TeamService;

@Service
public class TeamJoinRequestServiceImpl implements TeamRequestService {

	private TeamJoinRequestRepository teamJoinRequestRepo;
	private TeamRepository teamRepo;
	private UserRepository userRepo;
	private TeamService teamService;

	public TeamJoinRequestServiceImpl(TeamJoinRequestRepository teamRequestRepo, TeamRepository teamRepo,
			UserRepository userRepo, TeamService teamService) {
		this.teamJoinRequestRepo = teamRequestRepo;
		this.teamRepo = teamRepo;
		this.userRepo = userRepo;
		this.teamService = teamService;
	}

	@Override
	public Result requestToJoinTeam(String joinerEmail, UUID teamId, TeamJoinRequestDTO teamRequestDTO) {
		if (teamId == null || teamId.toString().isBlank()) {
			throw new RuntimeException("Team id is wrong");
		}
		byte[] teamIdArray = uuidToByteArray(teamId);
		
		if (teamJoinRequestRepo.existsByApplicantEmailAndTeamId(joinerEmail, teamIdArray)) {
			throw new RuntimeException("You already applied here");
		}

		Team appliedTo = teamRepo.findByStatusAndPublicId(TeamStatus.OPEN, teamIdArray);
		if (appliedTo == null) {
			throw new RuntimeException("Incorrect Team ID!");
		}

		UserEntity applicant = userRepo.findByEmail(joinerEmail);

		TeamJoinRequest teamJoinRequest = new TeamJoinRequest();

		UserEntity user = new UserEntity();
		user.setEmail(joinerEmail);
		teamJoinRequest.setApplicant(user);

		teamJoinRequest.setMessage(teamRequestDTO.getMessage());

		teamJoinRequest.setRequestedTeam(appliedTo);

		TeamJoinRequest requested = teamJoinRequestRepo.save(teamJoinRequest);

		return requested == null ? Result.FAILED : Result.SUCCUESS;
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

	public byte[] uuidToByteArray(UUID id) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(id.getMostSignificantBits());
		bb.putLong(id.getLeastSignificantBits());
		return bb.array();
	}
}
