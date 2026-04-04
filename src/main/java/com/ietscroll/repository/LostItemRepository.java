package com.ietscroll.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ietscroll.entity.LostItemEntity;
import com.ietscroll.response.Result;

@Repository
public interface LostItemRepository extends JpaRepository<LostItemEntity, Integer> {

	@Query(value = "SELECT * FROM lost_item WHERE owner_email = :email AND status='OPEN' ", nativeQuery = true)
	List<LostItemEntity> findActiveRequestByEmail(@Param("email") String email);

	@Transactional
	@Modifying
	@Query(value = "UPDATE lost_item SET status=CLOSED where status='OPEN' AND owner_email = :email AND public_id_of_lost_request = :idOfRequest", nativeQuery = true)
	Result closeRequest(@Param("email") String email, @Param("idOfRequest") UUID idOfRequest);

	@Query(value = "Select COUNT(*) from lost_item where owner_email = :email AND status = 'OPEN'", nativeQuery = true)
	int countRequestByUser(@Param("email") String email);
}
