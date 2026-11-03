package com.ietscroll.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ietscroll.entity.FoundItemEntity;
import com.ietscroll.general.enums.FoundItemStatus;

@Repository
public interface FoundItemRepository extends JpaRepository<FoundItemEntity, Integer> {

    @Query(value = "SELECT COUNT(*) FROM found_item WHERE contact_to = ?1 AND status = 'PENDING'", nativeQuery = true)
    int countByUser(String email);

    @Query(value = "SELECT COUNT(*) FROM found_item WHERE contact_to = ?1", nativeQuery = true)
    int countTotalByUser(String email);

    @Query(value = "SELECT * FROM found_item WHERE contact_to = ?1 AND status = 'PENDING'", nativeQuery = true)
    List<FoundItemEntity> findActiveRequestByEmail(String email);

    Page<FoundItemEntity> findByStatus(FoundItemStatus status, Pageable pageable);

    long countByStatus(FoundItemStatus status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE found_item SET status='CLOSE' WHERE status='PENDING' AND contact_to=?1 AND public_id_of_found_item=?2", nativeQuery = true)
    int closeRequest(String email, UUID publicId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE found_item SET status='CLOSE' WHERE public_id_of_found_item=?1", nativeQuery = true)
    int closeRequestByAdmin(UUID publicId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE found_item SET status='SUSPENDED' WHERE contact_to=?1 AND status='PENDING'", nativeQuery = true)
    int suspendFoundItemsByUser(String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE found_item SET status='PENDING' WHERE contact_to=?1 AND status='SUSPENDED'", nativeQuery = true)
    int restoreFoundItemsByUser(String email);

    @Query(value = "SELECT DATE(created_at) as reg_date, COUNT(*) as cnt FROM found_item WHERE created_at >= :startDate GROUP BY DATE(created_at) ORDER BY DATE(created_at) ASC", nativeQuery = true)
    List<Object[]> getFoundItemTimeline(@Param("startDate") LocalDateTime startDate);
}
