package com.olg.qweb.api.users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.olg.qweb.api.auth.dto.AuthRequest;
import com.olg.qweb.api.registration.dto.RegistrationRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Allows @BeforeAll to be non-static
class UserControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker = new Faker();

    private final String username = faker.name().username();
    private final String email = username + "@mail.com";
    private final String password = "password123";
    private String uid;
    String accessToken;

    @BeforeAll
    void registerAndLogin() throws Exception {
        RegistrationRequest request = new RegistrationRequest(username, email, password);
        mockMvc.perform(
                post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        // Perform login
        AuthRequest loginRequest = new AuthRequest(email, password);

        MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn();

        // Read access token from JSON response
        String json = loginResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        accessToken = root.get("accessToken").asText();

//         Decode JWT payload (assumes JWT format: header.payload.signature)
        String[] tokenParts = accessToken.split("\\.");
        String payloadJson = new String(Base64.getUrlDecoder().decode(tokenParts[1]), StandardCharsets.UTF_8);
//
        JsonNode payload = objectMapper.readTree(payloadJson);
        uid = payload.get("uid").asText(); // or whatever claim you expect
//
        System.out.println("Access token: " + accessToken);
        System.out.println("Decoded payload: " + payloadJson);
        System.out.println("UID: " + uid);

    }

    @Test
    void getUserById() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/api/users/" + uid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Api-Version", 1)
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("result: " + result.getResponse().getContentAsString());

//        ApiResponse<IUserResponse> apiResponse = objectMapper.readValue(
//                result.getResponse().getContentAsString(),
//                new TypeReference<ApiResponse<IUserResponse>>() {}
//        );
//
//        System.out.println("apiResponse: " + apiResponse);

    }
}