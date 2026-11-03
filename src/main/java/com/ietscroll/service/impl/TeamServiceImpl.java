package com.ietscroll.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ietscroll.dto.TeamDTO;
import com.ietscroll.entity.Skills;
import com.ietscroll.entity.Team;
import com.ietscroll.entity.TeamFinderSkill;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.exception.BadRequestException;
import com.ietscroll.exception.DuplicateResourceException;
import com.ietscroll.exception.ResourceNotFoundException;
import com.ietscroll.general.enums.Privacy;
import com.ietscroll.general.enums.TeamStatus;
import com.ietscroll.repository.SkillRepository;
import com.ietscroll.repository.TeamJoinRequestRepository;
import com.ietscroll.repository.TeamRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.Result;
import com.ietscroll.response.TeamResponse;
import com.ietscroll.service.SpamProtectionService;
import com.ietscroll.service.TeamService;
import com.ietscroll.service.UserActivityLogService;

@Service
public class TeamServiceImpl implements TeamService {

	private final TeamRepository teamRepo;
	private final TeamJoinRequestRepository teamJoinRequestRepo;
	private final SkillRepository skillRepository;
	private final SpamProtectionService spamProtectionService;
	private final UserRepository userRepo;
	private final ModelMapper modelMapper;
	private final UserActivityLogService activityLogService;

	public TeamServiceImpl(TeamRepository teamRepo, SkillRepository skillRepository,
			SpamProtectionService spamProtectionService, UserRepository userRepo,
			TeamJoinRequestRepository teamJoinRequestRepo, ModelMapper modelMapper,
			UserActivityLogService activityLogService) {
		this.teamRepo = teamRepo;
		this.skillRepository = skillRepository;
		this.spamProtectionService = spamProtectionService;
		this.userRepo = userRepo;
		this.teamJoinRequestRepo = teamJoinRequestRepo;
		this.modelMapper = modelMapper;
		this.activityLogService = activityLogService;
	}

	@Override
	@Transactional
	public TeamResponse createTeam(String ownerEmail, TeamDTO team) {

		if (ownerEmail == null || team == null) {
			throw new BadRequestException("Invalid details");
		}

		int teamsCreated = teamRepo.countByCreatedBy_EmailAndStatus(ownerEmail, TeamStatus.OPEN);
		if (teamsCreated >= 1) {
			throw new DuplicateResourceException("You can't create more than one team");
		}

		// ── Strict spam/content-safety check on team purpose ─────────────────
		try {
			spamProtectionService.checkText(team.getPurpose());
		} catch (Exception e) {
			activityLogService.logActivity(ownerEmail, "TEAM", team.getPurpose(), "REJECTED_BY_MODERATION", e.getMessage());
			throw e;
		}

		UserEntity user = userRepo.findByEmail(ownerEmail);

		Team teamEntity = new Team();
		teamEntity.setCreatedBy(user);
		teamEntity.setMaxMember(team.getMaxMember());
		teamEntity.setPurpose(team.getPurpose());
		teamEntity.setPrivacy(team.getPrivacy());

		List<Skills> skills = new ArrayList<>();
		if (team.getSkillIds() != null && !team.getSkillIds().isEmpty()) {
			skills = skillRepository.findAllById(team.getSkillIds());
			if (skills.size() != team.getSkillIds().size()) {
				throw new ResourceNotFoundException("Some skills not found");
			}
		}

		List<TeamFinderSkill> neededSkills = new ArrayList<>();

		for (Skills skill : skills) {
			TeamFinderSkill tfs = new TeamFinderSkill();
			tfs.setSkill(skill);
			tfs.setTeam(teamEntity);
			neededSkills.add(tfs);
		}

		teamEntity.setNeededSkills(neededSkills);

		Team createdTeam = teamRepo.save(teamEntity);

		activityLogService.logActivity(ownerEmail, "TEAM", team.getPurpose(), "UPLOADED", "Team created and publicly open for applications.");

		TeamResponse teamResponse = new TeamResponse();
		teamResponse.setCreatedAt(createdTeam.getCreatedAt());
		teamResponse.setCreatedBy(createdTeam.getCreatedBy().getEmail());
		teamResponse.setMaxMember(createdTeam.getMaxMember());
		teamResponse.setPurpose(createdTeam.getPurpose());
		teamResponse.setStatus(createdTeam.getStatus());
		teamResponse.setPrivacy(createdTeam.getPrivacy());
		teamResponse.setCurrentMember(createdTeam.getCurrentMember());
		teamResponse.setPublicId(createdTeam.getPublicId());
		if (user != null && user.getPrivilegeClass() != null) {
			teamResponse.setCreatorPrivilege(user.getPrivilegeClass().name());
		}

		return teamResponse;
	}

	@Override
	@Transactional
	public Result closeTeam(String ownerEmail) {
		if (ownerEmail == null) {
			throw new BadRequestException("Invalid credentials");
		}
		int result = teamRepo.closeTeam(ownerEmail);
		return result == 1 ? Result.SUCCESS : Result.FAILED;
	}

	@Override
	@Transactional
	public Result changeTeamSize(String ownerEmail, int teamSize) {

		if (teamSize <= 0 || teamSize > 20) {
			throw new BadRequestException("Team size should not less than zero and higher than twenty");
		}
		int count = teamRepo.updateTeamSize(ownerEmail, teamSize);

		return count == 1 ? Result.SUCCESS : Result.FAILED;
	}

	@Override
	public TeamResponse getTeamById(UUID publicId) {
		if (publicId == null || publicId.toString().isBlank()) {
			throw new BadRequestException("Invalid Id");
		}

		Team team = teamRepo.findByPublicId(publicId);

		if (team == null) {
			throw new ResourceNotFoundException("No valid team found with given team Id");
		}

		TeamResponse response = new TeamResponse();

		response.setCreatedAt(team.getCreatedAt());
		response.setCreatedBy(team.getCreatedBy().getEmail());
		response.setMaxMember(team.getMaxMember());
		response.setPurpose(team.getPurpose());
		response.setCurrentMember(team.getCurrentMember());
		response.setPrivacy(team.getPrivacy());
		if (team.getCreatedBy() != null && team.getCreatedBy().getPrivilegeClass() != null) {
			response.setCreatorPrivilege(team.getCreatedBy().getPrivilegeClass().name());
		}

		return response;
	}

	@Override
	public Page<TeamResponse> getActiveTeams(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		Page<Team> teams = teamRepo.findByStatusAndPrivacy(TeamStatus.OPEN, Privacy.PUBLIC, pageable);

		if (teams == null) {
			throw new ResourceNotFoundException("No active teams rightnow");
		}
		return teams.map(team -> {
			TeamResponse teamResponse = new TeamResponse();
			teamResponse.setCreatedBy(team.getCreatedBy().getEmail());
			teamResponse.setCreatedAt(team.getCreatedAt());
			teamResponse.setMaxMember(team.getMaxMember());
			teamResponse.setCurrentMember(team.getCurrentMember());
			teamResponse.setPublicId(team.getPublicId());
			teamResponse.setPurpose(team.getPurpose());
			teamResponse.setStatus(team.getStatus());
			teamResponse.setPrivacy(team.getPrivacy());
			if (team.getCreatedBy() != null && team.getCreatedBy().getPrivilegeClass() != null) {
				teamResponse.setCreatorPrivilege(team.getCreatedBy().getPrivilegeClass().name());
			} else {
				teamResponse.setCreatorPrivilege("NORMAL");
			}
			return teamResponse;
		});
	}

	@Override
	public TeamResponse getMyTeamDetails(String onwerEmail) {

		Team team = teamRepo.findByStatusAndCreatedBy_Email(TeamStatus.OPEN, onwerEmail);
		if (team == null) {
			throw new ResourceNotFoundException("Team doesn't exist");
		}
		TeamResponse teamResponse = modelMapper.map(team, TeamResponse.class);
		teamResponse.setCreatedBy(onwerEmail);
		if (team.getCreatedBy() != null && team.getCreatedBy().getPrivilegeClass() != null) {
			teamResponse.setCreatorPrivilege(team.getCreatedBy().getPrivilegeClass().name());
		}
		return teamResponse;

	}

	@Override
	public List<TeamResponse> getMyTeamPosts(String ownerEmail) {
		return null;
	}
}
