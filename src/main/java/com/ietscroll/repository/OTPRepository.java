package com.ietscroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ietscroll.entity.OTPEntity;

@Repository
public interface OTPRepository extends JpaRepository<OTPEntity,Integer>{
	List<OTPEntity> findByEmail(String email);
	
	@Query(value = "delete from otp where expiration_time < CURRENT_TIMESTAMP", nativeQuery=true)
	void deleteOldOTPs();
}
