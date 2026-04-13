package com.ietscroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ietscroll.entity.TeamJoinRequest;

@Repository
public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest,Integer>{

}
