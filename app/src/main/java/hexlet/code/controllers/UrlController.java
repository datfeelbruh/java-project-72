package hexlet.code.controllers;

import hexlet.code.App;
import hexlet.code.models.Url;
import hexlet.code.models.query.QUrl;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static hexlet.code.AppUtil.getNormalizedUrl;

public class UrlController {
    private static final Logger URL_CONTROLLER_LOGGER = LoggerFactory.getLogger(App.class);
    public static final Handler LIST_URLS = ctx -> {
        List<Url> urls = new QUrl()
                .orderBy()
                .id.asc()
                .findList();

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
}
