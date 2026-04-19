package com.ietscroll.service.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ietscroll.dto.TeamDTO;
import com.ietscroll.entity.Skills;
import com.ietscroll.entity.Team;
import com.ietscroll.entity.TeamFinderSkill;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.general.enums.Privacy;
import com.ietscroll.general.enums.TeamStatus;
import com.ietscroll.repository.SkillRepository;
import com.ietscroll.repository.TeamRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.Result;
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

		String isSafe = mistralChatClient.prompt().user(team.getPurpose()).call().content();

		if (!Boolean.parseBoolean(isSafe)) {
			throw new RuntimeException("Kindly maintain decorum!");
		}

		UserEntity user = userRepo.findByEmail(ownerEmail);

		if (user == null) {
			throw new UsernameNotFoundException("Email doesn't exist");
		}

		Team teamEntity = new Team();
		teamEntity.setCreatedBy(user);
		teamEntity.setMaxMember(team.getMaxMember());
		teamEntity.setPurpose(team.getPurpose());
		teamEntity.setPrivacy(team.getPrivacy());

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
	public Result closeTeam(String ownerEmail) {

		int count = teamRepo.closeTeam(ownerEmail);

		return count == 1 ? Result.SUCCUESS : Result.FAILED;
	}

	@Override
	public Result changeTeamSize(String ownerEmail, int teamSize) {

		if (teamSize <= 0 || teamSize > 20) {
			throw new RuntimeException("Team size should not less than zero and higher than twenty");
		}
		int count = teamRepo.updateTeamSize(ownerEmail, teamSize);

		return count == 1 ? Result.SUCCUESS : Result.FAILED;
	}

	@Override
	public TeamResponse getTeamById(UUID publicId) {
		if(publicId==null || publicId.toString().isBlank()) {
			throw new RuntimeException("Invalid Id");
		}
		
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(publicId.getMostSignificantBits());
		bb.putLong(publicId.getLeastSignificantBits());

		Team team=teamRepo.findByStatusAndPublicId(TeamStatus.OPEN, bb.array());

		if(team==null) {
			throw new RuntimeException("No valid team found with given team Id");
		}
		
		TeamResponse response = new TeamResponse();
		
		response.setCreatedAt(team.getCreatedAt());
		response.setCreatedBy(team.getCreatedBy().getEmail());
		response.setMaxMember(team.getMaxMember());
		response.setPurpose(team.getPurpose());
		response.setPrivacy(team.getPrivacy());
		
		return response;
	}

	@Override
	public Page<TeamResponse> getActiveTeams(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		return teamRepo.findByStatusAndPrivacy(TeamStatus.OPEN, Privacy.PUBLIC, pageable).map(team -> {
			TeamResponse teamResponse = new TeamResponse();
			teamResponse.setCreatedAt(team.getCreatedAt());
			teamResponse.setCreatedBy(team.getCreatedBy().getEmail());
			teamResponse.setMaxMember(team.getMaxMember());
			teamResponse.setPublicId(team.getPublicId());
			teamResponse.setPurpose(team.getPurpose());
			teamResponse.setStatus(team.getStatus());
			teamResponse.setPrivacy(team.getPrivacy());
			return teamResponse;
		});
	}

	@Override
	public TeamResponse getMyTeamDetails(String onwerEmail) {

		Team team = teamRepo.findByStatusAndCreatedBy_Email(TeamStatus.OPEN, onwerEmail);
		ModelMapper modelMapper = new ModelMapper();
		TeamResponse teamResponse = modelMapper.map(team, TeamResponse.class);
		teamResponse.setCreatedBy(onwerEmail);
		return teamResponse;

	}

	@Override
	public List<TeamResponse> getMyTeamPosts(String ownerEmail) {
		return null;
	}

	@Override
	public boolean isTeamExist(UUID publicId) {
		if(publicId==null || publicId.toString().isBlank()) {
			throw new RuntimeException("Invalid Id");
		}
		
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(publicId.getMostSignificantBits());
		bb.putLong(publicId.getLeastSignificantBits());
		
		return teamRepo.findByStatusAndPublicId(TeamStatus.OPEN,bb.array())==null ? false : true;
	}

}
