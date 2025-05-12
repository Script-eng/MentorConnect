package hu.nje.mentorconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.models.Meeting;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {

    public interface OnMeetingActionListener {
        void onEdit(Meeting meeting);
        void onDelete(Meeting meeting);
    }

    private List<Meeting> meetings;
    private boolean isMentor;
    private final OnMeetingActionListener actionListener;

    public MeetingAdapter(List<Meeting> meetings, boolean isMentor, OnMeetingActionListener actionListener) {
        this.meetings = meetings;
        this.isMentor = isMentor;
        this.actionListener = actionListener;
    }

    public void setMentor(boolean isMentor) {
        this.isMentor = isMentor;
        notifyDataSetChanged();
    }

    public void updateList(List<Meeting> newMeetings) {
        this.meetings = newMeetings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meeting, parent, false);
        return new MeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        Meeting meeting = meetings.get(position);
        holder.title.setText(meeting.getTitle());
        holder.location.setText("Location: " + meeting.getLocation());
        holder.purpose.setText("Purpose: " + meeting.getPurpose());

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        holder.date.setText("Date: " + formatter.format(meeting.getDate().toDate()));

        if (isMentor) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> actionListener.onEdit(meeting));
            holder.btnDelete.setOnClickListener(v -> actionListener.onDelete(meeting));
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return meetings != null ? meetings.size() : 0;
    }

    static class MeetingViewHolder extends RecyclerView.ViewHolder {
        TextView title, location, purpose, date;
        ImageButton btnEdit, btnDelete;

        MeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.meeting_title);
            location = itemView.findViewById(R.id.meeting_location);
            purpose = itemView.findViewById(R.id.meeting_purpose);
            date = itemView.findViewById(R.id.meeting_date);
            btnEdit = itemView.findViewById(R.id.meeting_edit_button);
            btnDelete = itemView.findViewById(R.id.meeting_delete_button);
        }
    }
}