package hexlet.code.controllers;

import hexlet.code.App;
import hexlet.code.models.Url;
import hexlet.code.models.UrlCheck;
import hexlet.code.models.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;


public class UrlController {
    private static final Logger URL_CONTROLLER_LOGGER = LoggerFactory.getLogger(App.class);
    public static final Handler LIST_URLS = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int urlPerPage = 12;
        int offset = (page - 1) * urlPerPage;

        PagedList<Url> pagedUrl = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(urlPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrl.getList();
        int currentPage = pagedUrl.getPageIndex() + 1;
        int lastPage = pagedUrl.getTotalPageCount() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .toList();

        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.attribute("urls", urls);

        ctx.render("urls/index.html");
    };

    public static final Handler ADD_URL = ctx -> {
        String normalizedUrl = getNormalizedUrl(ctx.formParam("url"));

        URL_CONTROLLER_LOGGER.info("Проверка что AppUtil смог получить корретный URL");

        if (Objects.isNull(normalizedUrl)) {
            URL_CONTROLLER_LOGGER.info("AppUtil не смог нормализовать URL. Некорректный URL");
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        URL_CONTROLLER_LOGGER.info("Проверка что такого URL {} еще нет в БД", normalizedUrl);

        Url databaseUrl = new QUrl()
                .name.equalTo(normalizedUrl)
                .findOne();

        if (Objects.nonNull(databaseUrl)) {
            URL_CONTROLLER_LOGGER.info("Такой URL {} уже существует в БД", normalizedUrl);
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
            return;
        }

        URL_CONTROLLER_LOGGER.info("URL {} прошел все проверки и будет добавлен", normalizedUrl);

        Url url = new Url(normalizedUrl);
        url.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static final Handler SHOW_URL = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (Objects.isNull(url)) {
            throw new NotFoundResponse();
        }

        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };
    public static final Handler CHECK_URL = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        URL_CONTROLLER_LOGGER.info("ID {} полученный из контекста", id);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (Objects.isNull(url)) {
            throw new NotFoundResponse();
        }

        URL_CONTROLLER_LOGGER.info("Попытка провести проверку URL {}", url);
        HttpResponse<String> response = Unirest
                .get(url.getName())
                .asString();

        int statusCode = response.getStatus();
        Document body = Jsoup.parse(response.getBody());
        String title = body.title();
        Element h1FromBody = body.selectFirst("h1");
        String h1 = Objects.nonNull(h1FromBody) ? h1FromBody.text() : null;
        Element descriptionFromBody = body.selectFirst("meta[name=description]");
        String description = Objects.nonNull(descriptionFromBody)
                ? descriptionFromBody.attr("content") : null;

        UrlCheck checkedUrl = new UrlCheck(statusCode, title, h1, description, url);
        URL_CONTROLLER_LOGGER.info("URL {} был проверен", url);

        checkedUrl.save();

        ctx.sessionAttribute("flash", "Страница успешно проверена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls/" + id);
    };

    public static String getNormalizedUrl(String url) {
        try {
            URL_CONTROLLER_LOGGER.info("Попытка нормализовать полученный URL {}", url);
            URL receivedUrl = new URL(url);

            String normalizedUrl = String.format("%s://%s", receivedUrl.getProtocol(), receivedUrl.getHost());

            if (receivedUrl.getPort() > 0) {
                normalizedUrl = normalizedUrl + ":" + receivedUrl.getPort();
            }

            URL_CONTROLLER_LOGGER.info("Получен URL {}", normalizedUrl);

            return normalizedUrl;

        } catch (MalformedURLException e) {
            return null;
        }
    }
}
