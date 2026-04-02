package com.ietscroll.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.response.QualityOfResume;
import com.ietscroll.service.ResumeCheckerService;

@Service
public class ResumeCheckerServiceImpl implements ResumeCheckerService {

	private ChatClient chatClient;

	public ResumeCheckerServiceImpl(ChatClient chatClient) {
		this.chatClient=chatClient;
	}

	@Override
	public QualityOfResume getQuality(MultipartFile file, String role, int experience) {
		System.out.println(extractTextFromFile(file));

		return chatClient.prompt()
		.user(extractTextFromFile(file))
		.system(sys->sys.params(Map.of("role",role,"experience",experience)))
		.call()
		.responseEntity(QualityOfResume.class)
		.entity();
	}

	private static String extractTextFromFile(MultipartFile file) {

		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File is empty or null");
		}
		try {
			TikaDocumentReader reader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));
			List<Document> documents = reader.get();

			if (documents == null || documents.isEmpty()) {
				return "";
			}

			StringBuilder content = new StringBuilder();

			for (Document doc : documents) {
				if (doc.getText() != null) {
					content.append(doc.getText()).append("\n");
				}
			}
			return content.toString().trim();

		} catch (IOException e) {
			throw new RuntimeException("Failed to read file");
		}
	}

}
