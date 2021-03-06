

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boltonadhesives.informatica.core.constants.BoltonConstants;
import com.boltonadhesives.informatica.core.service.HttpApiService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handles all logic for import and polling
 *
 *
 */
@Component(service = HttpApiService.class, immediate = true)
public class HttpApiServiceImpl implements HttpApiService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpApiServiceImpl.class);

  public HttpResponse makeHttpPostCall(String url, String username, String password, String input, int timeOut) {

    int CONNECTION_TIMEOUT_MS = timeOut * 1000; // Timeout in millis.
    RequestConfig requestConfig = RequestConfig.custom()
      .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
      .setConnectTimeout(CONNECTION_TIMEOUT_MS)
      .setSocketTimeout(CONNECTION_TIMEOUT_MS)
      .build();

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpPost postRequest = new HttpPost(url);

    postRequest.setConfig(requestConfig);

  //  String encoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
  //  postRequest.addHeader(HttpHeaders.ACCEPT, BoltonConstants.APPLICATION_JSON);
  //  postRequest.addHeader(HttpHeaders.CONTENT_TYPE, BoltonConstants.APPLICATION_JSON);
  //  postRequest.addHeader(HttpHeaders.AUTHORIZATION, BoltonConstants.BASIC + " " + encoding);

    try {
      StringEntity params = new StringEntity(input);
      postRequest.setEntity(params);
    } catch (UnsupportedEncodingException ex) {
      LOGGER.error("******UnsupportedEncodingException*******" + ex.getMessage());
    }
    HttpResponse response = null;

    try {
      response = httpClient.execute(postRequest);
    } catch (Exception e) {
      LOGGER.error("******Exception*******" + e.getMessage());
    }
    return response;

  }

  public HttpResponse makeHttpgetCall(String url, String username, String password, int timeOut, HashMap < String, String > headers) {

    int CONNECTION_TIMEOUT_MS = timeOut * 1000; // Timeout in millis.
    RequestConfig requestConfig = RequestConfig.custom()
      .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
      .setConnectTimeout(CONNECTION_TIMEOUT_MS)
      .setSocketTimeout(CONNECTION_TIMEOUT_MS)
      .build();

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpGet getRequest = new HttpGet(url);
    getRequest.setConfig(requestConfig);
    String encoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    //getRequest.addHeader("accept", "application/json");
    getRequest.addHeader(HttpHeaders.AUTHORIZATION, BoltonConstants.BASIC + " " + encoding);

    for (Object i: headers.keySet()) {
      getRequest.addHeader(i.toString(), headers.get(i).toString());
    }
    HttpResponse response = null;
    try {
      response = httpClient.execute(getRequest);
    } catch (Exception e) {
      LOGGER.error("******Exception*******" + e.getMessage());
    }

    return response;

  }

  public Reader getFilefromResponse(HttpResponse response) {
    Reader reader = null;
    try {
      reader = new InputStreamReader((response.getEntity().getContent()), "UTF-8");
    } catch (UnsupportedOperationException | IOException e) {
      LOGGER.error("******UnsupportedOperationException | IOException*******" + e.getMessage());
    }
    return reader;

  }

  public JsonObject getResponseJson(HttpResponse response) {
    JsonObject object = null;
    try {
      String content = EntityUtils.toString(response.getEntity());
      object = new JsonParser().parse(content).getAsJsonObject();
    } catch (ParseException | IOException e) {
      LOGGER.error("******ParseException | IOException*******" + e.getMessage());

    }
    return object;
  }
}