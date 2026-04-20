package com.ietscroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ietscroll.entity.TeamJoinRequest;
import com.ietscroll.general.enums.TeamRequestStatus;

@Repository
public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest, Integer> {

	@Query(value="SELECT COUNT(*) > 0 from team_join_requests where applicant_email=?1 AND team_id=?2",nativeQuery=true)
	int existsByEmailAndTeamPublicId(String email,  byte[] publicId);	
	
	
	List<TeamJoinRequest> findByRequestedTeam_CreatedBy_EmailAndStatus(String email,TeamRequestStatus Status);
	
	List<TeamJoinRequest> findByApplicant_Email(String applicantEmail);
	
	@Transactional
	@Modifying
	@Query(value="Update team_join_requests set status=?1 where  status='WAIT' AND applicant_email=?2 AND team_id=?3",nativeQuery=true)
	int changeStatusOfApplicant(String Status,String applicantEmail,byte [] teamId);
	
	
	@Transactional
	@Modifying
	@Query(value="Update team_join_requests set status='REMOVED' where  status='ACCEPTED' AND applicant_email=?2 AND team_id=?3",nativeQuery=true)
	int kickApplicant(String applicantEmail,byte [] teamId);
	
}
