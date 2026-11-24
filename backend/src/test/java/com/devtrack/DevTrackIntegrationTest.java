package com.devtrack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DevTrackIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String register(String email, boolean sample) throws Exception {
        String body = "{\"name\":\"Test\",\"email\":\"" + email + "\",\"password\":\"password123\",\"loadSampleData\":" + sample + "}";
        String res = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(res).get("token").asText();
    }

    private JsonNode getJson(String url, String token) throws Exception {
        String res = mvc.perform(get(url).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(res);
    }

    @Test
    void protectedEndpointsRequireLogin() throws Exception {
        mvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        register("login@test.com", false);
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"login@test.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"login@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void taskCompletionUpdatesDashboard() throws Exception {
        String token = register("tasks@test.com", false);

        String created = mvc.perform(post("/api/tasks").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Learn Streams\",\"subject\":\"Java\",\"priority\":\"HIGH\",\"estimatedMinutes\":30}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).get("id").asLong();

        assertEquals(1, getJson("/api/dashboard", token).get("tasksPendingToday").asInt());

        mvc.perform(patch("/api/tasks/" + id + "/toggle").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        JsonNode dash = getJson("/api/dashboard", token);
        assertEquals(1, dash.get("tasksCompletedToday").asInt());
        assertEquals(100, dash.get("productivityPercent").asInt());
        assertEquals(1, dash.get("streak").asInt());
    }

    @Test
    void dsaProblemsAndJavaTopicsWork() throws Exception {
        String token = register("dsa@test.com", false);
        mvc.perform(post("/api/dsa").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Two Sum\",\"platform\":\"LeetCode\",\"topic\":\"HASHING\",\"difficulty\":\"EASY\",\"status\":\"SOLVED\"}"))
                .andExpect(status().isCreated());
        assertEquals(1, getJson("/api/dsa/summary", token).get("totalSolved").asInt());

        JsonNode topics = getJson("/api/java-topics", token);
        assertEquals(12, topics.size());
        long topicId = topics.get(0).get("id").asLong();
        mvc.perform(put("/api/java-topics/" + topicId).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"completed\":true,\"notes\":\"done\"}"))
                .andExpect(status().isOk());
        assertTrue(getJson("/api/java-topics", token).get(0).get("completed").asBoolean());
    }

    @Test
    void usersCannotSeeEachOthersData() throws Exception {
        String a = register("a@test.com", true);
        String b = register("b@test.com", false);
        assertTrue(getJson("/api/tasks", a).size() > 0);
        assertEquals(0, getJson("/api/tasks", b).size());
    }

    @Test
    void projectsAndNotesWork() throws Exception {
        String token = register("proj@test.com", false);
        String p = mvc.perform(post("/api/projects").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"DevTrack\",\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long pid = json.readTree(p).get("id").asLong();
        mvc.perform(post("/api/projects/" + pid + "/tasks").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Backend\",\"done\":true}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/projects/" + pid + "/tasks").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Frontend\",\"done\":false}"))
                .andExpect(status().isCreated());
        assertEquals(50, getJson("/api/projects", token).get(0).get("completionPercent").asInt());

        mvc.perform(post("/api/notes").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Note\",\"content\":\"text\",\"category\":\"CODING\"}"))
                .andExpect(status().isCreated());
        assertEquals(1, getJson("/api/notes", token).size());
    }
}
