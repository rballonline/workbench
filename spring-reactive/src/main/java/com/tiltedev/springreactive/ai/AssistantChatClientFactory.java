package com.tiltedev.springreactive.ai;

import com.anthropic.models.messages.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

/**
 * Builds a {@link ChatClient} per request rather than once as a singleton bean, because the
 * assistant is bring-your-own-key: there is no server-side default {@code AnthropicChatModel} to
 * share (see {@code spring.autoconfigure.exclude} in {@code application.yml}), so each call needs
 * its own model built from the API key the caller supplied.
 */
@Component
@RequiredArgsConstructor
public class AssistantChatClientFactory {

  private static final String MODEL = "claude-sonnet-5";

  private static final String SYSTEM_PROMPT =
      """
      You are the travel assistant for "Places I'd Like to Visit", a shared travel wishlist app.
      Use the available tools to answer questions and act on the user's behalf: search cities,
      look up weather and country info, check the ISS position, and manage the shared wishlist.

      The wishlist is shared with other connected users. Adding a destination is safe and
      immediate. Removing one is not: only call propose_remove_destination when the user asks to
      remove something, and after calling it, tell the user the removal is pending confirmation
      in the UI - never say it has been removed, since only the user's confirmation click in the
      app actually deletes it.
      """;

  private final AssistantTools assistantTools;
  private final ChatMemory chatMemory;

  public ChatClient forApiKey(String apiKey) {
    var options = AnthropicChatOptions.builder().apiKey(apiKey).model(Model.of(MODEL)).build();
    var chatModel = AnthropicChatModel.builder().options(options).build();
    return ChatClient.builder(chatModel)
        .defaultSystem(SYSTEM_PROMPT)
        .defaultTools(assistantTools)
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .build();
  }
}
