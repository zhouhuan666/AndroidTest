package com.gdet.testapp.retrofit;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2024-04-20
 * 描述：
 */
public class SimpleService {

    public static final String API_URL = "https://api.github.com";

    private static final String TAG = "SimpleService";

    public static class Constributer {
        public final String login;

        public final int contributions;

        public Constributer(String login, int contributions) {
            this.login = login;
            this.contributions = contributions;
        }

        @Override
        public String toString() {
            return "Constributer{" +
                    "login='" + login + '\'' +
                    ", contributions=" + contributions +
                    '}';
        }
    }

    public interface Github {
        @GET("/repos/{owner}/{repo}/contributors")
        Call<List<Constributer>> contributors(@Path("owner") String owner, @Path("repo") String repo);
    }

    public void execute() throws IOException {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();

        Github github = retrofit.create(Github.class);

        Call<List<Constributer>> call = github.contributors("square", "retrofit");

        List<Constributer> contributors = call.execute().body();

        for (Constributer constributer : contributors) {
            Log.d(TAG, "execute: " + constributer);
        }

    }
}
