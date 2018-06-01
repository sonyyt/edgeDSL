package vt.cs.selab.androidhttpserver;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by LocalAdministrator on 1/9/2017.
 */
public class httpClient {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client;
    public httpClient(){
        client = new OkHttpClient();

    }

    public String run(String url) throws IOException {
        Request request = new Request.Builder()
                    .url(url)
                    .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}