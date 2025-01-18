package com.gdet.testapp.retrofit;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.mock.BehaviorDelegate;
import retrofit2.mock.MockRetrofit;
import retrofit2.mock.NetworkBehavior;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2024-04-20
 * 描述：
 */
public class SimpleMockService {
    private static final String TAG = "SimpleMockService";
    public static class MockGithub implements SimpleService.Github {

        private final BehaviorDelegate<SimpleService.Github> delegate;

        private final Map<String, Map<String, List<SimpleService.Constributer>>> ownerRepoContributors;

        public MockGithub(BehaviorDelegate<SimpleService.Github> delegate) {
            this.delegate = delegate;
            ownerRepoContributors = new LinkedHashMap<>();
            addContributor("square", "retrofit", "John Doe", 12);
            addContributor("square", "retrofit", "Bob Smith", 2);
            addContributor("square", "retrofit", "Big Bird", 40);
            addContributor("square", "picasso", "Proposition Joe", 39);
            addContributor("square", "picasso", "Keiser Soze", 152);
        }

        @Override

        public Call<List<SimpleService.Constributer>> contributors(String owner, String repo) {
            List<SimpleService.Constributer> response = Collections.emptyList();
            Map<String, List<SimpleService.Constributer>> repoContributors = ownerRepoContributors.get(owner);
            if (repoContributors != null) {
                List<SimpleService.Constributer> constributers = repoContributors.get(repo);
                if (constributers != null) {
                    response = constributers;
                }
            }
            return delegate.returningResponse(response).contributors(owner, repo);
        }

        void addContributor(String owner, String repo, String name, int contributions) {
            Map<String, List<SimpleService.Constributer>> repoContributors = ownerRepoContributors.get(owner);
            if (repoContributors == null) {
                repoContributors = new LinkedHashMap<>();
            }
            List<SimpleService.Constributer> constributers = repoContributors.get(repo);
            if (constributers == null) {
                constributers = new ArrayList<>();
                repoContributors.put(repo, constributers);
            }
            constributers.add(new SimpleService.Constributer(name, contributions));

        }




    }

    private static void printContributors(SimpleService.Github gitHub, String owner, String repo)
            throws IOException {
        Log.d(TAG,String.format("== Contributors for %s/%s ==", owner, repo));
        Call<List<SimpleService.Constributer>> contributors = gitHub.contributors(owner, repo);
        for (SimpleService.Constributer contributor : contributors.execute().body()) {
           Log.d(TAG,contributor.login + " (" + contributor.contributions + ")");
        }
    }

    public void execute() throws IOException {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(SimpleService.API_URL).build();

        NetworkBehavior behavior = NetworkBehavior.create();
        MockRetrofit mockRetrofit = new MockRetrofit.Builder(retrofit).networkBehavior(behavior).build();
        BehaviorDelegate<SimpleService.Github> delegate=mockRetrofit.create(SimpleService.Github.class);

        MockGithub github=new MockGithub(delegate);

        // Query for some contributors for a few repositories.
        printContributors(github, "square", "retrofit");
        printContributors(github, "square", "picasso");

        // Using the mock-only methods, add some additional data.
        System.out.println("Adding more mock data...\n");
        github.addContributor("square", "retrofit", "Foo Bar", 61);
        github.addContributor("square", "picasso", "Kit Kat", 53);

        // Reduce the delay to make the next calls complete faster.
        behavior.setDelay(500, TimeUnit.MILLISECONDS);

        // Query for the contributors again so we can see the mock data that was added.
        printContributors(github, "square", "retrofit");
        printContributors(github, "square", "picasso");


    }
}
