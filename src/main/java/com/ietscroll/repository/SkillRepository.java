package com.ietscroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ietscroll.entity.Skills;

@Repository
public interface SkillRepository extends JpaRepository<Skills,Integer>{
	
}
