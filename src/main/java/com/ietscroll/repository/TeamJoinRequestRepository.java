package com.ietscroll.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ietscroll.entity.TeamJoinRequest;

@Repository
public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest, Integer> {

	@Query(value="SELECT COUNT(*) > 0 from team_join_requests where applicant_email=?1 AND team_id=?2",nativeQuery=true)
	int existsByEmailAndTeamPublicId(String email,  byte[] publicId);	
//	List<TeamResponse> findByTeamIdAndStatus(byte[] teamId,TeamRequestStatus teamRequestStatus.);

}
