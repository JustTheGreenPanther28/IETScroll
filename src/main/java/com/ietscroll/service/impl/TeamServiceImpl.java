package com.ietscroll.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ietscroll.dto.TeamDTO;
import com.ietscroll.dto.TeamJoinRequestDTO;
import com.ietscroll.entity.Skills;
import com.ietscroll.entity.Team;
import com.ietscroll.entity.TeamFinderSkill;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.general.enums.TeamStatus;
import com.ietscroll.repository.SkillRepository;
import com.ietscroll.repository.TeamRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.Result;
import com.ietscroll.response.TeamJoinResponse;
import com.ietscroll.response.TeamResponse;
import com.ietscroll.service.TeamService;

@Service
public class TeamServiceImpl implements TeamService {

	private TeamRepository teamRepo;
	private UserRepository userRepo;
	private SkillRepository skillRepository;
	private ChatClient mistralChatClient;

	public TeamServiceImpl(TeamRepository teamRepo, UserRepository userRepo, SkillRepository skillRepository,
			@Qualifier("mistralChatClient") ChatClient mistralChatClient) {
		this.teamRepo = teamRepo;
		this.userRepo = userRepo;
		this.skillRepository = skillRepository;
		this.mistralChatClient = mistralChatClient;
	}

	@Override
	public TeamResponse createTeam(String ownerEmail, TeamDTO team) {

		if (ownerEmail == null || team == null) {
			throw new RuntimeException("Invalid details");
		}

		int teamsCreated = teamRepo.countByCreatedBy_EmailAndStatus(ownerEmail, TeamStatus.OPEN);
		if (teamsCreated >= 1) {
			throw new RuntimeException("You can't create more than one team");
		}

		String isSafe = mistralChatClient
				.prompt()
				.user(team.getPurpose())
				.call()
				.content();

		if (!Boolean.parseBoolean(isSafe)) {
			throw new RuntimeException("Please be respectful");
		}

		UserEntity user = userRepo.findByEmail(ownerEmail);
		if(user==null) {
			throw new UsernameNotFoundException("Email doesn't exist");
		}

		Team teamEntity = new Team();
		teamEntity.setCreatedBy(user);
		teamEntity.setMaxMember(team.getMaxMember());
		teamEntity.setPurpose(team.getPurpose());

		List<Skills> skills = skillRepository.findAllById(team.getSkillIds());

		if (skills.size() != team.getSkillIds().size()) {
			throw new RuntimeException("Some skills not found");
		}

		List<TeamFinderSkill> neededSkills = new ArrayList<>();

		for (Skills skill : skills) {
			TeamFinderSkill tfs = new TeamFinderSkill();
			tfs.setSkill(skill);
			tfs.setTeam(teamEntity);
			neededSkills.add(tfs);
		}

		teamEntity.setNeededSkills(neededSkills);

		teamEntity = teamRepo.save(teamEntity);

		TeamResponse teamResponse = new TeamResponse();
		teamResponse.setCreatedAt(teamEntity.getCreatedAt());
		teamResponse.setCreatedBy(teamEntity.getCreatedBy().getEmail());
		teamResponse.setMaxMember(teamEntity.getMaxMember());
		teamResponse.setPublicId(teamEntity.getPublicId());
		teamResponse.setPurpose(teamEntity.getPurpose());
		teamResponse.setStatus(teamEntity.getStatus());
		return teamResponse;
	}

	@Override
	public List<TeamResponse> getActiveTeams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TeamResponse> getMyApplications(String joinerEmail) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TeamResponse changeTeamSize(String ownerEmail, int teamSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TeamResponse closeTeam(String ownerEmail) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TeamResponse> getMyTeamPosts(String ownerEmail) {
		// TODO Auto-generated method stub
		return null;
	}

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

}
