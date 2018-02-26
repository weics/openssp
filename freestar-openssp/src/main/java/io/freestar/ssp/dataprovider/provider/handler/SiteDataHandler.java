package io.freestar.ssp.dataprovider.provider.handler;

import com.atg.openssp.core.cache.broker.dto.SiteDto;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import io.freestar.ssp.dataprovider.provider.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class SiteDataHandler extends DataHandler {
    private static final Logger log = LoggerFactory.getLogger(SiteDataHandler.class);
    public static final String CONTEXT = "/lookup/site";

    public SiteDataHandler(HttpServletRequest request, HttpServletResponse response) {
        try {
            Gson gson = new Gson();
            String content = new String(Files.readAllBytes(Paths.get("site_db.json")), StandardCharsets.UTF_8);
            SiteDto data = gson.fromJson(content, SiteDto.class);

            Map<String,String> parms = queryToMap(request.getQueryString());
            String t = parms.get("t");

            if (LoginService.TOKEN.equals(t)) {
                String result = new Gson().toJson(data);

                response.setStatus(200);
                response.setContentType("application/json; charset=UTF8");
                OutputStream os = response.getOutputStream();
                os.write(result.getBytes());
                os.close();
            } else {
                response.setStatus(401);
            }
        } catch (IOException e) {
            response.setStatus(500);
            log.error(e.getMessage(), e);
        }
    }

    public SiteDataHandler(HttpExchange httpExchange) {
        try {
            Gson gson = new Gson();
            String content = new String(Files.readAllBytes(Paths.get("site_db.json")), StandardCharsets.UTF_8);
            SiteDto data = gson.fromJson(content, SiteDto.class);

            Map<String,String> parms = queryToMap(httpExchange.getRequestURI().getQuery());

            String t = parms.get("t");
            if (LoginService.TOKEN.equals(t)) {
                String result = new Gson().toJson(data);
                log.debug("sending: "+result);
                httpExchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF8");
                httpExchange.sendResponseHeaders(200, result.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(result.getBytes());
                os.close();
            } else {
                httpExchange.sendResponseHeaders(401, 0);
            }
        } catch (IOException e) {
            try {
                httpExchange.sendResponseHeaders(500, 0);
            } catch (IOException e1) {
            }
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void cleanUp() {

    }

}
