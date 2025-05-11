package hu.nje.mentorconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.models.Document;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private final List<Document> documentList;
    private final OnDownloadClickListener downloadClickListener;
    private final OnDeleteClickListener   deleteClickListener;
    //Adding the Mentor Bool that we can control the delete button
    private final boolean isMentor;


    public interface OnDownloadClickListener {
        void onDownloadClick(Document document);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Document document);
    }

    // ONE constructor, taking both listeners
    public DocumentAdapter(
            List<Document> documentList,
            OnDownloadClickListener downloadClickListener,
            OnDeleteClickListener   deleteClickListener,
             boolean isMentor
    ) {
        this.documentList        = documentList;
        this.downloadClickListener = downloadClickListener;
        this.deleteClickListener   = deleteClickListener;
        this.isMentor = isMentor;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        Document doc = documentList.get(position);
        holder.titleTextView.setText(doc.getTitle());

        holder.downloadButton.setOnClickListener(v ->
                downloadClickListener.onDownloadClick(doc)
        );

        if (isMentor) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v ->
                    deleteClickListener.onDeleteClick(doc)
            );
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageButton downloadButton;
        ImageButton deleteButton;

        DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView    = itemView.findViewById(R.id.document_title_text);
            downloadButton   = itemView.findViewById(R.id.download_button);
            deleteButton     = itemView.findViewById(R.id.delete_button);
        }
    }
}