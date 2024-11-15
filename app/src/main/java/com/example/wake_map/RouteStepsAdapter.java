package com.example.wake_map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RouteStepsAdapter extends RecyclerView.Adapter<RouteStepsAdapter.RouteStepViewHolder> {
    private List<String> routeSteps;

    public RouteStepsAdapter(List<String> routeSteps) {
        this.routeSteps = routeSteps;
    }

    @NonNull
    @Override
    public RouteStepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_step_item, parent, false);
        return new RouteStepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteStepViewHolder holder, int position) {
        holder.stepTextView.setText(routeSteps.get(position));
    }

    @Override
    public int getItemCount() {
        return routeSteps.size();
    }

    static class RouteStepViewHolder extends RecyclerView.ViewHolder {
        TextView stepTextView;

        public RouteStepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepTextView = itemView.findViewById(R.id.stepTextView);
        }
    }
}
