package com.ietscroll.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ietscroll.entity.UserEntity;
import com.ietscroll.general.enums.PrivilegeClass;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

	UserEntity findByEmail(String email);

	UserEntity findByUsername(String username);

	long countByIsBlocked(boolean isBlocked);

	long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

	@Query("SELECT u FROM users u WHERE " +
		   "(:query IS NULL OR :query = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
		   "(:privilege IS NULL OR u.privilegeClass = :privilege) AND " +
		   "(:isBlocked IS NULL OR u.isBlocked = :isBlocked)")
	Page<UserEntity> searchUsers(@Param("query") String query,
								 @Param("privilege") PrivilegeClass privilege,
								 @Param("isBlocked") Boolean isBlocked,
								 Pageable pageable);

	@Query("SELECT DATE(u.createdAt) as regDate, COUNT(u) as cnt FROM users u WHERE u.createdAt >= :startDate GROUP BY DATE(u.createdAt) ORDER BY DATE(u.createdAt) ASC")
	List<Object[]> getUserGrowthTimeline(@Param("startDate") LocalDateTime startDate);
}
