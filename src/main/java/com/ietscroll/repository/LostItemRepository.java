package com.ietscroll.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ietscroll.entity.LostItemEntity;
import com.ietscroll.general.enums.LostItemStatus;

@Repository
public interface LostItemRepository extends JpaRepository<LostItemEntity, Integer> {

	@Query(value = "SELECT * FROM lost_item WHERE owner_email = :email AND status='OPEN' ", nativeQuery = true)
	List<LostItemEntity> findActiveRequestByEmail(@Param("email") String email);

	@Modifying
	@Transactional
	@Query(value = "UPDATE lost_item SET status='CLOSED' where status='OPEN' AND owner_email = ? AND public_id_of_lost_request = ?", nativeQuery = true)
	int closeRequest(String email, byte[]idOfRequest);

	@Query(value = "Select COUNT(*) from lost_item where owner_email = :email AND status = 'OPEN'", nativeQuery = true)
	int countRequestByUser(@Param("email") String email);
	
	
	Page<LostItemEntity> findByStatus(LostItemStatus lostItemStatus,Pageable pageable);

}
