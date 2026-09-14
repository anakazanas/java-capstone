package assembly.general.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationLifecycleIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String patronToken;
    private String librarianToken;

    @BeforeEach
    void setUp() throws Exception {
        String uniqueEmail = "patron" + System.nanoTime() + "@example.com";

        postJson("/api/auth/register", String.format(
                "{\"email\":\"%s\",\"password\":\"SecurePass123!\",\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"phoneNumber\":\"+1-555-0123\"}",
                uniqueEmail));

        ResponseEntity<String> loginResponse = postJson("/api/auth/login",
                String.format("{\"email\":\"%s\",\"password\":\"SecurePass123!\"}", uniqueEmail));
        patronToken = objectMapper.readTree(loginResponse.getBody()).get("accessToken").asText();

        ResponseEntity<String> librarianLogin = postJson("/api/auth/login",
                "{\"email\":\"librarian@library.com\",\"password\":\"Librarian123!\"}");
        librarianToken = objectMapper.readTree(librarianLogin.getBody()).get("accessToken").asText();
    }

    @Test
    void fullReserveCheckoutReturnLifecycle() throws Exception {
        ResponseEntity<String> catalog = restTemplate.getForEntity("/api/catalog/books?query=Clean Code", String.class);
        String bookId = objectMapper.readTree(catalog.getBody()).get("content").get(0).get("bookId").asText();

        ResponseEntity<String> reserveResponse = postJsonAuth("/api/reservations",
                "{\"bookId\":\"" + bookId + "\"}", patronToken);
        assertThat(reserveResponse.getStatusCode().value()).isEqualTo(201);
        JsonNode reserveJson = objectMapper.readTree(reserveResponse.getBody());
        assertThat(reserveJson.get("status").asText()).isEqualTo("RESERVED");
        String reservationId = reserveJson.get("reservationId").asText();

        ResponseEntity<String> patronCheckoutAttempt = postJsonAuth(
                "/api/reservations/" + reservationId + "/checkout", "{}", patronToken);
        assertThat(patronCheckoutAttempt.getStatusCode().value()).isEqualTo(403);

        ResponseEntity<String> checkoutResponse = postJsonAuth(
                "/api/reservations/" + reservationId + "/checkout", "{\"notes\":\"Good\"}", librarianToken);
        assertThat(checkoutResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(objectMapper.readTree(checkoutResponse.getBody()).get("status").asText()).isEqualTo("CHECKED_OUT");

        ResponseEntity<String> returnResponse = postJsonAuth(
                "/api/reservations/" + reservationId + "/return",
                "{\"condition\":\"GOOD\",\"notes\":\"fine\"}", librarianToken);
        assertThat(returnResponse.getStatusCode().value()).isEqualTo(200);
        JsonNode returnJson = objectMapper.readTree(returnResponse.getBody());
        assertThat(returnJson.get("lateDays").asInt()).isZero();
        assertThat(returnJson.get("lateFee").asDouble()).isEqualTo(0.00);
    }

    @Test
    void sixthActiveReservationIsRejected() throws Exception {
        ResponseEntity<String> catalog = restTemplate.getForEntity("/api/catalog/books?size=10", String.class);
        JsonNode books = objectMapper.readTree(catalog.getBody()).get("content");

        for (int i = 0; i < 5; i++) {
            String bookId = books.get(i).get("bookId").asText();
            ResponseEntity<String> response = postJsonAuth("/api/reservations",
                    "{\"bookId\":\"" + bookId + "\"}", patronToken);
            assertThat(response.getStatusCode().value()).isEqualTo(201);
        }

        String sixthBookId = books.get(5).get("bookId").asText();
        ResponseEntity<String> sixthResponse = postJsonAuth("/api/reservations",
                "{\"bookId\":\"" + sixthBookId + "\"}", patronToken);

        assertThat(sixthResponse.getStatusCode().value()).isEqualTo(400);
        assertThat(objectMapper.readTree(sixthResponse.getBody()).get("error").asText())
                .isEqualTo("RESERVATION_LIMIT_EXCEEDED");
    }

    private ResponseEntity<String> postJson(String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(path, new HttpEntity<>(body, headers), String.class);
    }

    private ResponseEntity<String> postJsonAuth(String path, String body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return restTemplate.postForEntity(path, new HttpEntity<>(body, headers), String.class);
    }
}