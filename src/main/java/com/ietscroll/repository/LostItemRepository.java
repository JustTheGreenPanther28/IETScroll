package com.ietscroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ietscroll.entity.LostItemEntity;

@Repository
public interface LostItemRepository extends JpaRepository<LostItemEntity,Integer	>{

}
