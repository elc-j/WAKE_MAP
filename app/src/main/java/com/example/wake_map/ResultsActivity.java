package com.example.wake_map;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends AppCompatActivity {
    private RecyclerView routeStepsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);

        routeStepsRecyclerView = findViewById(R.id.routeStepsRecyclerView);
        routeStepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get route data from Intent
        String routeData = getIntent().getStringExtra("routeData");
        if (routeData != null) {
            List<String> steps = parseRouteSteps(routeData);
            RouteStepsAdapter adapter = new RouteStepsAdapter(steps);
            routeStepsRecyclerView.setAdapter(adapter);
        }
    }

    private List<String> parseRouteSteps(String routeData) {
        List<String> steps = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(routeData);
            JSONArray legs = jsonObject.getJSONArray("legs");
            for (int i = 0; i < legs.length(); i++) {
                JSONArray stepsArray = legs.getJSONObject(i).getJSONArray("steps");
                for (int j = 0; j < stepsArray.length(); j++) {
                    String instruction = stepsArray.getJSONObject(j).getString("html_instructions");
                    steps.add(instruction.replaceAll("<[^>]*>", "")); // Remove HTML tags
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return steps;
    }
}
