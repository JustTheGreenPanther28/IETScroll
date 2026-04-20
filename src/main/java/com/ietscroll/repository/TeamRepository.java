package com.ietscroll.repository;

import java.util.UUID;

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

    int countByCreatedBy_EmailAndStatus(String email, TeamStatus status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE team_finder SET status='CLOSED' WHERE status='OPEN' AND created_by=?1", nativeQuery = true)
    int closeTeam(String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE team_finder SET max_member = ?2 WHERE status='OPEN' AND created_by=?1", nativeQuery = true)
    int updateTeamSize(String email, int teamSize);

    Team findByStatusAndCreatedBy_Email(TeamStatus status, String email);

    @Query(value="SELECT * FROM team_finder WHERE status='OPEN' AND public_id=?1", nativeQuery=true)
    Team findByPublicId(UUID publicId);

    Page<Team> findByStatusAndPrivacy(TeamStatus status, Privacy privacy, Pageable pageable);
}