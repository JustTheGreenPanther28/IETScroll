package com.ietscroll.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ietscroll.entity.FoundItemEntity;
import com.ietscroll.general.enums.FoundItemStatus;

@Repository
public interface FoundItemRepository extends JpaRepository<FoundItemEntity, Integer> {

	@Query(value = "Select Count(*) from found_item where contact_to= ? AND status= 'PENDING'", nativeQuery = true)
	int countByUser(String email);

	@Query(value = "Select * from found_item where contact_to = ? AND status = 'PENDING'", nativeQuery = true)
	List<FoundItemEntity> findActiveRequestByEmail(String email);

	Page<FoundItemEntity> findByStatus(FoundItemStatus lostItemStatus, Pageable pageable);

	@Modifying
	@Transactional
	@Query(value = "UPDATE found_item set status='CLOSE' where status='PENDING' email=? AND public_id_of_found_item=?", nativeQuery = true)
	int closeRequest(String email, byte[] array);

}
