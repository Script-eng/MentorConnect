package hu.nje.mentorconnect.fragments;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.adapters.DocumentAdapter;
import hu.nje.mentorconnect.models.Document;

public class DocsFragment extends Fragment implements DocumentAdapter.OnDownloadClickListener {

    private static final String TAG = "DocsFragment";

    private RecyclerView documentsRecyclerView;
    private DocumentAdapter documentAdapter;
    private List<Document> documentList;
    private TextView generalInfoTextView;
    private Document pendingDownloadDocument = null;

    // To keep track of downloads initiated by this fragment session (optional but good practice)
    // Map<DownloadId, Filename>
    private final Map<Long, String> activeDownloads = new HashMap<>();


    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission granted.");
                    if (pendingDownloadDocument != null) {
                        startDownload(pendingDownloadDocument);
                    }
                } else {
                    Log.w(TAG, "WRITE_EXTERNAL_STORAGE permission denied.");
                    Toast.makeText(getContext(), R.string.storage_permission_denied, Toast.LENGTH_LONG).show();
                }
                pendingDownloadDocument = null; // Clear pending request regardless of outcome
            });

    public DocsFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_documents, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        documentsRecyclerView = view.findViewById(R.id.documents_recycler_view);
        generalInfoTextView = view.findViewById(R.id.general_info_text);

        documentList = new ArrayList<>();
        documentAdapter = new DocumentAdapter(documentList, this);

        documentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        documentsRecyclerView.setAdapter(documentAdapter);

        loadDocuments();
    }

    private void loadDocuments() {
        documentList.clear();
        // Sample Data - Replace with your actual data source
        documentList.add(new Document("Mentoring Handbook", "Guidelines and best practices.", "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"));
        documentList.add(new Document("Program Schedule", "Timeline of key events.", "https://www.clickdimensions.com/links/TestPDFfile.pdf"));
        documentList.add(new Document("Contact List", "Important contact information.", "https://www.africau.edu/images/default/sample.pdf"));
        documentList.add(new Document("Feedback Form", "Template for feedback.", "https://raw.githubusercontent.com/mozilla/pdf.js/ba2edeae/web/compressed.tracemonkey-pldi-09.pdf"));
        documentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDownloadClick(Document document) {
        Log.i(TAG, "Download requested for: " + document.getTitle());

        if (!isNetworkAvailable()) {
            Toast.makeText(getContext(), R.string.no_network_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isDownloadManagerEnabled()) {
            Toast.makeText(getContext(), R.string.download_manager_disabled, Toast.LENGTH_LONG).show();
            // Optional: Intent to open download manager settings
            // try {
            //    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            //    intent.setData(Uri.parse("package:com.android.providers.downloads"));
            //    startActivity(intent);
            // } catch (Exception e) { Log.e(TAG, "Could not open DM settings", e); }
            return;
        }

        pendingDownloadDocument = document;
        checkPermissionAndDownload();
    }

    private boolean isNetworkAvailable() {
        if (getContext() == null) return false;
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private boolean isDownloadManagerEnabled() {
        if (getContext() == null) return false;
        try {
            int state = getContext().getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) { // Constants changed slightly in API 19
                return !(state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                        state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER ||
                        state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED);
            } else {
                return !(state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                        state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking DownloadManager state", e);
            return false; // Assume disabled if check fails
        }
    }

    private void checkPermissionAndDownload() {
        if (pendingDownloadDocument == null || getContext() == null) {
            Log.w(TAG, "checkPermissionAndDownload called with null document or context.");
            return;
        }

        // Permission needed only for API <= 28
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Storage permission already granted (API <= 28).");
                startDownload(pendingDownloadDocument);
                pendingDownloadDocument = null;
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.w(TAG, "Storage permission rationale should be shown (API <= 28).");
                // Explain why you need the permission (e.g., in a dialog) then launch request.
                // For simplicity here, we just request directly.
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            else {
                Log.d(TAG, "Requesting storage permission (API <= 28).");
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        } else {
            // No explicit storage permission needed for DownloadManager on API 29+ to public Downloads dir
            Log.d(TAG, "No explicit storage permission needed for DownloadManager (API 29+).");
            startDownload(pendingDownloadDocument);
            pendingDownloadDocument = null;
        }
    }


    private void startDownload(Document document) {
        if (document.getDownloadUrl() == null || document.getDownloadUrl().isEmpty() || getContext() == null) {
            Toast.makeText(getContext(), R.string.download_failed_toast, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "startDownload failed: Invalid URL or null context.");
            return;
        }

        DownloadManager downloadManager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) {
            Toast.makeText(getContext(), R.string.download_failed_toast, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "startDownload failed: DownloadManager service not found.");
            return;
        }

        try {
            Uri uri = Uri.parse(document.getDownloadUrl());
            String fileName = extractFileName(uri, document.getTitle());

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(document.getTitle());
            request.setDescription("Downloading document...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            // Mime type can be helpful but often handled automatically
            // request.setMimeType(getMimeType(document.getDownloadUrl()));


            long downloadId = downloadManager.enqueue(request);
            activeDownloads.put(downloadId, fileName); // Store the download reference

            Toast.makeText(getContext(), getString(R.string.download_started_toast, fileName), Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Download enqueued. ID: " + downloadId + ", Filename: " + fileName + ", URL: " + document.getDownloadUrl());

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error starting download: Invalid URI? " + document.getDownloadUrl(), e);
            Toast.makeText(getContext(), "Download failed: Invalid URL", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Log.e(TAG, "Error starting download for " + document.getTitle(), e);
            Toast.makeText(getContext(), R.string.download_failed_toast, Toast.LENGTH_SHORT).show();
        }
    }

    private String extractFileName(Uri uri, String fallbackTitle) {
        String result = null;
        try {
            if (uri != null && "content".equals(uri.getScheme())) {
                // Try to query content resolver if needed (not typical for DownloadManager source URIs)
            } else if (uri != null) {
                result = uri.getLastPathSegment();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error parsing URI for filename: " + uri, e);
        }


        if (result == null || result.isEmpty() || result.contains("=") || result.contains("?")) { // Basic check for invalid segment
            // Sanitize fallback title and add a common extension (heuristic)
            String sanitizedTitle = fallbackTitle.replaceAll("[^a-zA-Z0-9\\.\\-]", "_").replaceAll("_+", "_");
            String extension = ".pdf"; // Default assumption
            if (uri != null && uri.getPath() != null) {
                String path = uri.getPath().toLowerCase();
                if (path.endsWith(".docx")) extension = ".docx";
                else if (path.endsWith(".xlsx")) extension = ".xlsx";
                else if (path.endsWith(".pptx")) extension = ".pptx";
                else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) extension = ".jpg";
                else if (path.endsWith(".png")) extension = ".png";
                else if (path.endsWith(".txt")) extension = ".txt";
            }
            result = sanitizedTitle + extension;
            Log.d(TAG, "Using fallback filename generation: " + result);
        } else {
            Log.d(TAG, "Extracted filename from URI: " + result);
        }

        // Ensure filename is not excessively long (optional filesystem safeguard)
        if (result.length() > 100) {
            result = result.substring(result.length() - 100);
            Log.w(TAG, "Truncated long filename to: " + result);
        }

        return result;
    }

    // Optional: Helper to guess MIME type (can be inaccurate)
     /*
     private String getMimeType(String url) {
         String type = null;
         String extension = MimeTypeMap.getFileExtensionFromUrl(url);
         if (extension != null) {
             type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
         }
         Log.d(TAG, "Guessed MIME type for " + url + ": " + type);
         return type;
     }
     */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up references if needed, though downloads continue in background
        activeDownloads.clear();
        Log.d(TAG, "onDestroyView: Cleared active download references.");
    }
}