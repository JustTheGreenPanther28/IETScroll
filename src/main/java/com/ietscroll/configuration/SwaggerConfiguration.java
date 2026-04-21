package com.ietscroll.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfiguration {

    private final Cloudinary cloudinary;

    SwaggerConfiguration(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

	@Bean
	public OpenAPI myConfiguration() {

		// Contact
		Contact contact = new Contact();
		contact.setName("IET Scroll Developer Team");
		contact.setEmail("iet.scroll.in@gmail.com");

		License license = new License();
		license.setName("Student Project - Not Officially Affiliated");
		license.setUrl("https://github.com/JustTheGreenPanther28");

		// Info
		Info info = new Info();
		info.setTitle("IET Scroll API (Student Project)");
		info.setDescription(
				"This is a student-built backend application for managing unoffical problem in college like : lost & found items, team searching and formation for project, hackathon, and resume analysis. This project is NOT officially affiliated with IET DAVV.");
		info.setVersion("v1.0.0");
		info.setContact(contact);
		info.setLicense(license);

		ExternalDocumentation externalDocs = new ExternalDocumentation();
		externalDocs.setDescription("Project Repository");
		externalDocs.setUrl("https://github.com/JustTheGreenPanther28?tab=repositories");

		OpenAPI openAPI = new OpenAPI();
		openAPI.setInfo(info);
		openAPI.setExternalDocs(externalDocs);
		
		
		openAPI.setServers(List.of(new Server().url("https://iet-scroll.onrender.com").description("PUBLIC"),new Server().url("http://localhost:4040").description("PRIVATE")));

		return openAPI;
	}
}