package hu.nje.mentorconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.models.Mentor;

public class MentorAdapter extends RecyclerView.Adapter<MentorAdapter.MentorViewHolder> {

    private final List<Mentor> mentorList;
    private final OnMentorClickListener clickListener;

    // Interface for click events
    public interface OnMentorClickListener {
        void onMentorClick(Mentor mentor);
    }

    public MentorAdapter(List<Mentor> mentorList, OnMentorClickListener clickListener) {
        this.mentorList = mentorList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MentorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mentor, parent, false);
        return new MentorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MentorViewHolder holder, int position) {
        Mentor mentor = mentorList.get(position);
        holder.name.setText(mentor.getName());
        holder.department.setText(mentor.getDepartment());

        // Handle clicks
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onMentorClick(mentor);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mentorList.size();
    }

    public static class MentorViewHolder extends RecyclerView.ViewHolder {
        TextView name, department;

        public MentorViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.mentor_name);
            department = itemView.findViewById(R.id.mentor_major);
        }
    }
}