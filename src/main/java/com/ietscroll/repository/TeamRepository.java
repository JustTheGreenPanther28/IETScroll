package com.ietscroll.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ietscroll.entity.Team;
import com.ietscroll.general.enums.Privacy;
import com.ietscroll.general.enums.TeamStatus;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {

	int countByCreatedBy_EmailAndStatus(String email, TeamStatus teamStatus);

	@Modifying
	@Transactional
	@Query(value = "Update team_finder set status='CLOSED' where status='OPEN' AND created_by=?1", nativeQuery = true)
	int closeTeam(String email);

	@Modifying
	@Transactional
	@Query(value = "Update team_finder set max_member = ?2 where status='OPEN' AND created_by=?1", nativeQuery = true)
	int updateTeamSize(String email, int teamSize);
	
	Team findByStatusAndCreatedBy_Email(TeamStatus teamStatus,String email);

	Page<Team> findByStatusAndPrivacy(TeamStatus status, Privacy privacy, Pageable pageable);

}
