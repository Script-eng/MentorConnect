package hu.nje.mentorconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.models.Mentor;

public class MentorAdapter extends RecyclerView.Adapter<MentorAdapter.MentorViewHolder> {

    private List<Mentor> mentorList;

    public MentorAdapter(List<Mentor> mentorList) {
        this.mentorList = mentorList;
    }

    @NonNull
    @Override
    public MentorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mentor, parent, false);
        return new MentorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MentorViewHolder holder, int position) {
        Mentor mentor = mentorList.get(position);
        holder.name.setText(mentor.getName());
        holder.department.setText(mentor.getMajor());
        holder.image.setImageResource(mentor.getImageResId());
    }

    @Override
    public int getItemCount() {
        return mentorList.size();
    }

    public static class MentorViewHolder extends RecyclerView.ViewHolder {
        TextView name, department;
        ImageView image;

        public MentorViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.mentor_name);
            department = itemView.findViewById(R.id.mentor_major);
            image = itemView.findViewById(R.id.mentor_image);
        }
    }
}