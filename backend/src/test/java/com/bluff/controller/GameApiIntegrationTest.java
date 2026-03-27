package com.bluff.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postGames_returns201WithIds() throws Exception {
        mockMvc.perform(
                        post("/games")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content("{\"name\":\"alice\",\"cpuCount\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").isString())
                .andExpect(jsonPath("$.playerId").isString());
    }

    @Test
    void postGames_missingCpuCount_returns400() throws Exception {
        mockMvc.perform(
                        post("/games")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content("{\"name\":\"only\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postGames_invalidCpuCount_returns400() throws Exception {
        mockMvc.perform(
                        post("/games")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content("{\"cpuCount\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGame_unknown_returns404() throws Exception {
        mockMvc.perform(get("/games/no-such-game")).andExpect(status().isNotFound());
    }

    @Test
    void getGame_afterStart_exposesDiceCountNotDiceArray() throws Exception {
        String res =
                mockMvc.perform(
                                post("/games")
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .content("{\"cpuCount\":1}"))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        JsonNode n = objectMapper.readTree(res);
        String gameId = n.get("gameId").asText();
        String playerId = n.get("playerId").asText();

        mockMvc.perform(
                        post("/games/" + gameId + "/start")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content("{\"playerId\":\"" + playerId + "\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/games/" + gameId).param("viewerPlayerId", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players").isArray())
                .andExpect(jsonPath("$.players[0].diceCount").exists())
                .andExpect(jsonPath("$.players[0].dice").doesNotExist());
    }

    @Test
    void startTwice_returns409() throws Exception {
        String res =
                mockMvc.perform(
                                post("/games")
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .content("{\"cpuCount\":1}"))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        JsonNode n = objectMapper.readTree(res);
        String gameId = n.get("gameId").asText();
        String playerId = n.get("playerId").asText();

        mockMvc.perform(
                        post("/games/" + gameId + "/start")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content("{\"playerId\":\"" + playerId + "\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        post("/games/" + gameId + "/start")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content("{\"playerId\":\"" + playerId + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void postJoin_returns200() throws Exception {
        String res =
                mockMvc.perform(
                                post("/games")
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .content("{\"cpuCount\":1}"))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        String gameId = objectMapper.readTree(res).get("gameId").asText();

        mockMvc.perform(
                        post("/games/" + gameId + "/join")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content("{\"name\":\"guest\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").isString());
    }

    @Test
    void listGames_returns200() throws Exception {
        mockMvc.perform(get("/games")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }
}
