package com.ietscroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ietscroll.entity.Team;
import com.ietscroll.general.enums.TeamStatus;

@Repository
public interface TeamRepository extends JpaRepository<Team,Integer>{
	
	int countByCreatedBy_EmailAndStatus(String email,TeamStatus teamStatus);
}
