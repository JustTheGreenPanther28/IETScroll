package com.ietscroll.configuration;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

@Configuration
public class AIConfiguration {

	private final List<String> badWords = List.of("badwords");

	@Bean
	@Primary
	ChatClient chatClient(@Qualifier("openAiChatModel") ChatModel chatModel, ChatMemory chatMemory,
			@Value("classpath:/prompts/DefaultSystemPrompt.st") Resource systemPromptResource) {

		return ChatClient.builder(chatModel)
				.defaultAdvisors(new SafeGuardAdvisor(badWords))
				.defaultSystem(systemPromptResource).build();
	}
	
	@Bean
	ChatClient gemmaChatClient(
	        @Qualifier("gemmaChatModel") ChatModel chatModel,
	        ChatMemory chatMemory) {

	    return ChatClient.builder(chatModel)
	            .defaultAdvisors(new SafeGuardAdvisor(badWords))
	            .build();
	}

	@Bean
	ChatModel gemmaChatModel(@Value("${spring.ai.openai.api-key}") String apiKey) {
		OpenAiApi gemmaApi = OpenAiApi
				.builder()
				.baseUrl("https://integrate.api.nvidia.com/v1")
				.apiKey(apiKey)
				.build();
		
		return OpenAiChatModel.builder()
				.openAiApi(gemmaApi)
				.defaultOptions(OpenAiChatOptions.builder().model("google/gemma-4-31b-it").build())
				.build();
	}
	
	

	@Bean
	ChatMemory chatMemory() {
		return MessageWindowChatMemory.builder().build(); // keeps last 20 messages by default
	}
}
