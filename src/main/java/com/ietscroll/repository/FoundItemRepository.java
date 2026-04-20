package com.ietscroll.repository;
import java.util.List;
import java.util.UUID;

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

    @Query(value = "SELECT COUNT(*) FROM found_item WHERE contact_to = ?1 AND status = 'PENDING'", nativeQuery = true)
    int countByUser(String email);

    @Query(value = "SELECT * FROM found_item WHERE contact_to = ?1 AND status = 'PENDING'", nativeQuery = true)
    List<FoundItemEntity> findActiveRequestByEmail(String email);

    Page<FoundItemEntity> findByStatus(FoundItemStatus status, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "UPDATE found_item SET status='CLOSE' WHERE status='PENDING' AND contact_to=?1 AND public_id_of_found_item=?2", nativeQuery = true)
    int closeRequest(String email, UUID publicId);
}