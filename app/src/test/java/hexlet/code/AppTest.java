package hexlet.code;

import hexlet.code.models.Url;
import hexlet.code.models.query.QUrl;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Юнит тесты для всего приложения")
public final class AppTest {
    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl = "http://localhost:";
    private static final String CORRECT_URL_FOR_TESTS = "https://ru.hexlet.io";
    private static final String INCORRECT_URL_FOR_TESTS = "com:www:http";
    private static Database database;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        baseUrl += app.port();
        database = DB.getDefault();

    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        database.truncate("url");
    }

    @Nested
    @DisplayName("Юнит тест для корневого обработчика")
    class RootTest {
        @Test
        @DisplayName("Тест корневого обработчика")
        void indexTest() {
            HttpResponse<String> response = Unirest.get(baseUrl).asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Анализатор страниц");
        }
    }

    @Nested
    @DisplayName("Юнит тесты для обработчика URL")
    class UrlTest {
        @BeforeEach
        void addLinkBeforeTests() {
            Url url = new Url(CORRECT_URL_FOR_TESTS);
            url.save();
        }
        @Test
        @DisplayName("Тест обработчика выводящего все страницы")
        void testUrlIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
            String responseBody = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(responseBody).contains(CORRECT_URL_FOR_TESTS);
        }

        @Test
        @DisplayName("Тест обработчика страницы конректного URL")
        void testUrlShow() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls/" + 1).asString();
            String responseBody = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(responseBody).contains(CORRECT_URL_FOR_TESTS);
        }

        @Test
        @DisplayName("Тест функционала добавления корректного URL")
        void testAddUrl() {
            String url = "https://www.youtube.com";

            HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                    .field("url", url)
                    .asString();

            assertThat(response.getStatus()).isEqualTo(302);
            assertThat(response.getHeaders().getFirst("Location")).isEqualTo("/urls");

            response = Unirest.get(baseUrl + "/urls").asString();
            String responseBody = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(responseBody).contains(url);
            assertThat(responseBody).contains("Страница успешно добавлена");

            Url actualUrl = new QUrl()
                    .name.equalTo(url)
                    .findOne();

            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getName()).isEqualTo(url);
        }

        @Test
        @DisplayName("Тест функционала добавления некорректного URL")
        void addIncorrectUrl() {
            HttpResponse<String> response = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", INCORRECT_URL_FOR_TESTS)
                    .asString();

            assertThat(response.getStatus()).isEqualTo(302);
            assertThat(response.getHeaders().getFirst("Location")).isEqualTo("/");

            response = Unirest
                    .get(baseUrl + "/")
                    .asString();
            String responseBody = response.getBody();

            assertThat(responseBody).contains("Некорректный URL");

            Url url = new QUrl()
                    .name.equalTo(INCORRECT_URL_FOR_TESTS)
                    .findOne();

            assertThat(url).isNull();
        }

        @Test
        @DisplayName("Тест функционала добавления уже существующего URL")
        void addExistedUrl() {
            HttpResponse<String> response = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", CORRECT_URL_FOR_TESTS)
                    .asString();

            assertThat(response.getHeaders().getFirst("Location")).isEqualTo("/urls");

            response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(body).contains(CORRECT_URL_FOR_TESTS);
            assertThat(body).contains("Страница уже существует");
        }
    }
}
