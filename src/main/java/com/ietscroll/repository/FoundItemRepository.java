package com.ietscroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ietscroll.entity.FoundItemEntity;

@Repository
public interface FoundItemRepository extends JpaRepository<FoundItemEntity,Integer>{
	
}
