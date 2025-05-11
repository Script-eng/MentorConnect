package hu.nje.mentorconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.models.Mentee;

public class MenteeAdapter extends RecyclerView.Adapter<MenteeAdapter.MenteeViewHolder> {

    private List<Mentee> mentees;

    public MenteeAdapter(List<Mentee> mentees) {
        this.mentees = mentees;
    }

    @NonNull
    @Override
    public MenteeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mentee, parent, false);
        return new MenteeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenteeViewHolder holder, int position) {
        Mentee mentee = mentees.get(position);
        holder.name.setText(mentee.getName());
        holder.email.setText(mentee.getEmail());
        holder.assigned.setText("Assigned to: " + mentee.getAssignedMentorId());
    }

    @Override
    public int getItemCount() {
        return mentees.size();
    }

    public static class MenteeViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, assigned;

        public MenteeViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.mentee_name);
            email = itemView.findViewById(R.id.mentee_email);
            assigned = itemView.findViewById(R.id.mentee_assigned);
        }
    }
}