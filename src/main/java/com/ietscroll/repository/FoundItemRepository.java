package com.ietscroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ietscroll.entity.FoundItemEntity;

@Repository
public interface FoundItemRepository extends JpaRepository<FoundItemEntity, Integer> {

	@Query(value = "Select Count(*) from found_Item where contact_To= ?", nativeQuery = true)
	int countByUser(String email);

	@Query(value = "Select * from found_Item where contact_To = ? AND status = 'OPEN'", nativeQuery = true)
	List<FoundItemEntity> findActiveRequestByEmail(String email);
}
