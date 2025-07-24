// src/main/java/com/codezone/backend/ProblemController.java
package com.codezone.backend;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

// Define a simple DTO for incoming chat messages (from frontend)
class FrontendChatMessage {
    public String text;
    public String sender; // "user" or "ai"
}


@RestController
public class ProblemController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // --- NEW: Read the Judge0 URL from an environment variable ---
    // This allows us to change it easily for local vs. deployed environments.
    @Value("${judge0.api.url}")
    private String judge0ApiUrl;

    private final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ProblemController() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // Helper method to execute code on Judge0 and get output
    private String executeCodeOnJudge0(String sourceCode, int languageId, String stdin) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("source_code", Base64.getEncoder().encodeToString(sourceCode.getBytes("UTF-8")));
        requestBody.put("language_id", languageId);
        requestBody.put("stdin", Base64.getEncoder().encodeToString(stdin.getBytes("UTF-8")));

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        
        // Use the configurable URL
        String submissionUrl = judge0ApiUrl + "/submissions?base64_encoded=true&wait=true";

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(submissionUrl, request, String.class);
            JsonNode root = objectMapper.readTree(Objects.requireNonNull(response.getBody()));

            if (root.hasNonNull("stdout")) {
                return new String(Base64.getDecoder().decode(root.path("stdout").asText()));
            } else if (root.hasNonNull("stderr")) {
                return "Error: " + new String(Base64.getDecoder().decode(root.path("stderr").asText()));
            } else if (root.hasNonNull("compile_output")) {
                return "Compile Error: " + new String(Base64.getDecoder().decode(root.path("compile_output").asText()));
            } else if (root.hasNonNull("message")) {
                 return "Message: " + new String(Base64.getDecoder().decode(root.path("message").asText()));
            } else if (root.hasNonNull("status")) {
                return "Status: " + root.path("status").path("description").asText();
            }
            return "Execution finished with no output.";

        } catch (Exception e) {
            System.err.println("Error during Judge0 execution: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Failed to communicate with self-hosted Judge0: " + e.getMessage(), e);
        }
    }

    // AI Assistant Chatbot Endpoint (no changes needed here)
    @PostMapping("/api/gemini/chat")
    public ResponseEntity<String> chatWithGemini(@RequestBody List<FrontendChatMessage> chatHistory) {
        // ... this method remains the same
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", geminiApiKey);

        if (chatHistory == null || chatHistory.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chat history cannot be empty.");
        }

        ArrayNode contents = objectMapper.createArrayNode();
        for (FrontendChatMessage message : chatHistory) {
            ObjectNode contentPart = objectMapper.createObjectNode();
            contentPart.put("role", message.sender.equals("user") ? "user" : "model");
            
            ArrayNode partsArray = objectMapper.createArrayNode();
            ObjectNode textPart = objectMapper.createObjectNode();
            textPart.put("text", message.text);
            partsArray.add(textPart);
            
            contentPart.set("parts", partsArray);
            contents.add(contentPart);
        }

        FrontendChatMessage lastUserMessage = chatHistory.get(chatHistory.size() - 1);
        if (!lastUserMessage.sender.equals("user")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Last message in history must be from user.");
        }
        contents.remove(contents.size() - 1);

        ObjectNode newContentPart = objectMapper.createObjectNode();
        newContentPart.put("role", "user");
        ArrayNode newPartsArray = objectMapper.createArrayNode();
        ObjectNode newTextPart = objectMapper.createObjectNode();
        newTextPart.put("text", lastUserMessage.text + "\n\n" + "Please format your response using Markdown.");
        newPartsArray.add(newTextPart);
        newContentPart.set("parts", newPartsArray);
        contents.add(newContentPart);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("contents", contents);

        try {
            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL + geminiApiKey, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return ResponseEntity.status(response.getStatusCode()).body("Gemini API call failed: " + response.getBody());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");

            if (!textNode.isMissingNode()) {
                return ResponseEntity.ok(textNode.asText());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Gemini API did not return text content.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to call Gemini API for chat: " + e.getMessage());
        }
    }

    // Endpoint for proxying Judge0 code execution (no changes needed here)
    @PostMapping("/api/judge0/execute")
    public ResponseEntity<String> executeUserCode(@RequestBody Map<String, Object> submissionRequest) {
        try {
            String sourceCode = (String) submissionRequest.get("source_code");
            int languageId = ((Number) submissionRequest.get("language_id")).intValue();
            String stdin = (String) submissionRequest.get("stdin");

            String output = executeCodeOnJudge0(sourceCode, languageId, stdin);
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            System.err.println("Error proxying Judge0 request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Failed to execute code on Judge0: " + e.getMessage());
        }
    }
}
