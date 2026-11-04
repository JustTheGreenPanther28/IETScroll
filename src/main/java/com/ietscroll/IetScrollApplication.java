package com.ietscroll;

import java.util.TimeZone;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class IetScrollApplication {

	@Value("${server.admin.email:vedantmaheshwari2006@gmail.com}")
	private String adminEmail;

	@PostConstruct
	public void init() {
		// Set default timezone to Indian Standard Time (IST)
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
	}

	@Bean
	ApplicationRunner databaseMigrationRunner(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				// 1. Drop stale role check constraint (Hibernate may have created it without SENIOR_ADMIN)
				jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
				// Recreate with all valid roles so SENIOR_ADMIN is always accepted
				jdbcTemplate.execute(
					"ALTER TABLE users ADD CONSTRAINT users_role_check " +
					"CHECK (role::text = ANY (ARRAY['STUDENT'::text, 'ADMIN'::text, 'SENIOR_ADMIN'::text]))"
				);

				// 2. Fix privilege_class constraint — drop stale one and recreate without legacy values
				jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_privilege_class_check");
				jdbcTemplate.execute(
					"ALTER TABLE users ADD CONSTRAINT users_privilege_class_check " +
					"CHECK (privilege_class::text = ANY (ARRAY['TRUSTED'::text, 'MAY_BE_SPAM'::text, 'NEW'::text, 'NORMAL'::text]))"
				);

				// 3. Migrate any stale legacy privilege_class values
				jdbcTemplate.execute("UPDATE users SET privilege_class = 'TRUSTED' WHERE privilege_class::text IN ('ADMIN', 'SENIOR_ADMIN')");

				// 4. Ensure the designated admin account has SENIOR_ADMIN role
				if (adminEmail != null && !adminEmail.isBlank()) {
					jdbcTemplate.execute("UPDATE users SET role = 'SENIOR_ADMIN' WHERE LOWER(email) = LOWER('" + adminEmail.trim() + "')");
				}

				// 5. Allow optional image on lost_item and found_item
				try {
					jdbcTemplate.execute("ALTER TABLE lost_item ALTER COLUMN imageurl DROP NOT NULL");
				} catch (Exception ignored) {}
				try {
					jdbcTemplate.execute("ALTER TABLE lost_item ALTER COLUMN image_url DROP NOT NULL");
				} catch (Exception ignored) {}
				try {
					jdbcTemplate.execute("ALTER TABLE lost_item DROP CONSTRAINT IF EXISTS lost_item_imageurl_key");
				} catch (Exception ignored) {}
				try {
					jdbcTemplate.execute("ALTER TABLE lost_item DROP CONSTRAINT IF EXISTS lost_item_image_url_key");
				} catch (Exception ignored) {}

				try {
					jdbcTemplate.execute("ALTER TABLE found_item ALTER COLUMN imageurl DROP NOT NULL");
				} catch (Exception ignored) {}
				try {
					jdbcTemplate.execute("ALTER TABLE found_item ALTER COLUMN image_url DROP NOT NULL");
				} catch (Exception ignored) {}
				try {
					jdbcTemplate.execute("ALTER TABLE found_item DROP CONSTRAINT IF EXISTS found_item_imageurl_key");
				} catch (Exception ignored) {}
				try {
					jdbcTemplate.execute("ALTER TABLE found_item DROP CONSTRAINT IF EXISTS found_item_image_url_key");
				} catch (Exception ignored) {}

				System.out.println("DB migration: role/privilege and image constraints reset successfully.");
			} catch (Exception e) {
				System.err.println("Database migration notice: " + e.getMessage());
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(IetScrollApplication.class, args);
	}
	
	@Bean
	BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	ModelMapper modelMapper() {
		return new ModelMapper();
	}

}
