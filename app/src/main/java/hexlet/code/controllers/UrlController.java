package hexlet.code.controllers;

import hexlet.code.App;
import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static hexlet.code.AppUtil.getNormalizedUrl;


public class UrlController {
    private static final Logger urlControllerLogger = LoggerFactory.getLogger(App.class);

    public static final Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        final int urlsPerPage = 10;
        urlControllerLogger.info("Запрашиваем список URLs");
        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * urlsPerPage)
                .setMaxRows(urlsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;

        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .toList();

        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);

        ctx.render("urls/index.html");
    };

    public static Handler addUrl = ctx -> {
        String checkedUrl = getNormalizedUrl(ctx.formParam("url"));

        if (Objects.isNull(checkedUrl)) {
            urlControllerLogger.info("Некорретный URL{}", (Object) null);
            ctx.sessionAttribute("flash", "Некорретный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        urlControllerLogger.info("Ищем URL{} в базе данных", checkedUrl);

        Url existUrl = new QUrl()
                .name.equalTo(checkedUrl)
                .findOne();

        if (existUrl != null) {
            urlControllerLogger.info("Найден существующий URL {}", checkedUrl);
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/urls");
            return;
        }

        urlControllerLogger.info("URL{} прошел все проверки и будет добавлен", checkedUrl);
        Url url = new Url(checkedUrl);
        url.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static Handler showUrl = ctx -> {
//        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
//
//        urlControllerLogger.info("ID {} был найден, ищется совпадение в базе данных", id);
//        Url url = new QUrl
//                .id.equalTo(id)
//                .findOne();
//        if (url == null) {
//            throw new NotFoundResponse();
//        }
//
//        ctx.attribute("url", url);
//        ctx.render("show.html");
    };
}
