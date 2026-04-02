package com.ietscroll.configuration;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class AIConfiguration {

	private final List<String> badWords = List.of("damn", "hell", "shit", "fuck", "bitch", "asshole", "idiot", "stupid",
			"moron", "bastard", "mc", "bkl", "bc", "bkc");

	@Bean
	ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory,
			@Value("classpath:/prompts/DefaultSystemPrompt.st") Resource systemPromptResource) {


		return ChatClient.builder(chatModel).defaultAdvisors(new SafeGuardAdvisor(badWords))
				.defaultSystem(systemPromptResource).build();
	}
	
	@Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();  // keeps last 20 messages by default
    }
}
