package com.ietscroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ietscroll.entity.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team,Integer>{
	
}
