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

import com.ietscroll.entity.LostItemEntity;
import com.ietscroll.general.enums.LostItemStatus;

@Repository
public interface LostItemRepository extends JpaRepository<LostItemEntity, Integer> {

    @Query(value = "SELECT * FROM lost_item WHERE owner_email = :email AND status='OPEN'", nativeQuery = true)
    List<LostItemEntity> findActiveRequestByEmail(@Param("email") String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE lost_item SET status='CLOSED' WHERE status='OPEN' AND owner_email = ?1 AND public_id_of_lost_request = ?2", nativeQuery = true)
    int closeRequest(String email, UUID idOfRequest);

    @Modifying
    @Transactional
    @Query(value = "UPDATE lost_item SET status='CLOSED' WHERE public_id_of_lost_request = ?1", nativeQuery = true)
    int closeRequestByAdmin(UUID idOfRequest);

    @Modifying
    @Transactional
    @Query(value = "UPDATE lost_item SET status='SUSPENDED' WHERE owner_email = ?1 AND status='OPEN'", nativeQuery = true)
    int suspendLostItemsByUser(String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE lost_item SET status='OPEN' WHERE owner_email = ?1 AND status='SUSPENDED'", nativeQuery = true)
    int restoreLostItemsByUser(String email);

    @Query(value = "SELECT COUNT(*) FROM lost_item WHERE owner_email = :email AND status = 'OPEN'", nativeQuery = true)
    int countRequestByUser(@Param("email") String email);

    @Query(value = "SELECT COUNT(*) FROM lost_item WHERE owner_email = :email", nativeQuery = true)
    int countTotalByUser(@Param("email") String email);

    Page<LostItemEntity> findByStatus(LostItemStatus status, Pageable pageable);

    long countByStatus(LostItemStatus status);

    @Query(value = "SELECT DATE(created_at) as reg_date, COUNT(*) as cnt FROM lost_item WHERE created_at >= :startDate GROUP BY DATE(created_at) ORDER BY DATE(created_at) ASC", nativeQuery = true)
    List<Object[]> getLostItemTimeline(@Param("startDate") LocalDateTime startDate);
}
