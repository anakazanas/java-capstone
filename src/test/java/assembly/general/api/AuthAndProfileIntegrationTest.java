package assembly.general.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthAndProfileIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void duplicateEmailRegistrationReturns400() throws Exception {
        String email = "dup" + System.nanoTime() + "@example.com";
        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"SecurePass123!\",\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"phoneNumber\":\"+1-555-0123\"}",
                email);

        postJson("/api/auth/register", body);
        ResponseEntity<String> secondAttempt = postJson("/api/auth/register", body);

        assertThat(secondAttempt.getStatusCode().value()).isEqualTo(400);
        assertThat(objectMapper.readTree(secondAttempt.getBody()).get("error").asText())
                .isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void wrongPasswordLoginReturns401() throws Exception {
        String email = "wrongpw" + System.nanoTime() + "@example.com";
        postJson("/api/auth/register", String.format(
                "{\"email\":\"%s\",\"password\":\"SecurePass123!\",\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"phoneNumber\":\"+1-555-0123\"}",
                email));

        ResponseEntity<String> loginAttempt = postJson("/api/auth/login",
                String.format("{\"email\":\"%s\",\"password\":\"WrongPassword1!\"}", email));

        assertThat(loginAttempt.getStatusCode().value()).isEqualTo(401);
        assertThat(objectMapper.readTree(loginAttempt.getBody()).get("error").asText())
                .isEqualTo("AUTHENTICATION_FAILED");
    }

    @Test
    void profileWithoutTokenReturns401() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/users/profile", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void profileWithValidTokenReturnsStats() throws Exception {
        String email = "profile" + System.nanoTime() + "@example.com";
        postJson("/api/auth/register", String.format(
                "{\"email\":\"%s\",\"password\":\"SecurePass123!\",\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"phoneNumber\":\"+1-555-0123\"}",
                email));

        ResponseEntity<String> loginResponse = postJson("/api/auth/login",
                String.format("{\"email\":\"%s\",\"password\":\"SecurePass123!\"}", email));
        String token = objectMapper.readTree(loginResponse.getBody()).get("accessToken").asText();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> profileResponse = restTemplate.exchange(
                "/api/users/profile", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(profileResponse.getStatusCode().value()).isEqualTo(200);
        JsonNode json = objectMapper.readTree(profileResponse.getBody());
        assertThat(json.get("email").asText()).isEqualTo(email);
        assertThat(json.get("activeReservations").asInt()).isZero();
    }

    @Test
    void bookDetailNotFoundReturns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/catalog/books/00000000-0000-0000-0000-000000000000", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void bookDetailMalformedIdReturns404NotServerError() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/catalog/books/not-a-uuid", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void historyEndpointReturnsPagedEmptyListForNewUser() throws Exception {
        String email = "history" + System.nanoTime() + "@example.com";
        postJson("/api/auth/register", String.format(
                "{\"email\":\"%s\",\"password\":\"SecurePass123!\",\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"phoneNumber\":\"+1-555-0123\"}",
                email));

        ResponseEntity<String> loginResponse = postJson("/api/auth/login",
                String.format("{\"email\":\"%s\",\"password\":\"SecurePass123!\"}", email));
        String token = objectMapper.readTree(loginResponse.getBody()).get("accessToken").asText();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> historyResponse = restTemplate.exchange(
                "/api/reservations/history", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(historyResponse.getStatusCode().value()).isEqualTo(200);
        JsonNode json = objectMapper.readTree(historyResponse.getBody());
        assertThat(json.get("totalElements").asInt()).isZero();
    }

    private ResponseEntity<String> postJson(String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(path, new HttpEntity<>(body, headers), String.class);
    }
}