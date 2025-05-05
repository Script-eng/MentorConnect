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
import hu.nje.mentorconnect.models.Document; // Make sure this import is correct

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<Document> documentList;
    private OnDownloadClickListener downloadClickListener;

    // Interface for handling download clicks (implemented by DocsFragment)

    // Constructor
    public DocumentAdapter(List<Document> documentList,
                           OnDownloadClickListener listener) {
        this.documentList = documentList;
        this.downloadClickListener = listener;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_document, parent, false); // Ensure list_item_document.xml exists
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        // Get the document at the current position
        Document document = documentList.get(position);

        // Bind data to the views in the ViewHolder
        holder.titleTextView.setText(document.getTitle());

        // Set the click listener for the download button
        holder.downloadButton.setOnClickListener(v -> {
            if (downloadClickListener != null) {
                // Delegate the click event to the fragment/activity
                downloadClickListener.onDownloadClick(document);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the list
        return documentList == null ? 0 : documentList.size();
    }

    // ViewHolder class holding the views for each item
    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageButton downloadButton;

        DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.document_title_text);
            downloadButton = itemView.findViewById(R.id.download_button);
        }
    }

    // Optional: Method to update the list if data changes dynamically
    public void updateData(List<Document> newDocumentList) {
        this.documentList.clear();
        if (newDocumentList != null) {
            this.documentList.addAll(newDocumentList);
        }
        notifyDataSetChanged(); // Notify the adapter that the data set has changed
    }



    public interface OnDownloadClickListener {
        void onDownloadClick(Document document);
    }





}
