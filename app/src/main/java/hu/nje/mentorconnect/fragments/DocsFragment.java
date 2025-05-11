package hu.nje.mentorconnect.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.adapters.DocumentAdapter;
import hu.nje.mentorconnect.models.Document;


public class DocsFragment extends Fragment
        implements DocumentAdapter.OnDownloadClickListener,
        DocumentAdapter.OnDeleteClickListener {


    private static final int REQUEST_WRITE_STORAGE = 1001;
    private Document pendingDocument;
    private static final String TAG = "DocsFragment";
    private static final int PICK_FILE_REQUEST = 100;

    private RecyclerView documentsRecyclerView;
    private DocumentAdapter documentAdapter;
    private List<Document> documentList;
    private TextView generalInfoTextView;
    private boolean isMentor = false;
    private Button uploadButton;
    private Uri selectedFileUri;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private EditText documentTitleInput;

    public DocsFragment() {}

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
        uploadButton = view.findViewById(R.id.upload_button);
        documentTitleInput = view.findViewById(R.id.document_title_input);
        uploadButton.setVisibility(View.GONE);

        documentList = new ArrayList<>();
        documentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 🔧 Initialize Firebase instances FIRST
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        boolean isMentor = doc.exists() && "Mentor".equalsIgnoreCase(doc.getString("role"));

                        if (isMentor) {
                            uploadButton.setVisibility(View.VISIBLE);
                            uploadButton.setOnClickListener(v -> openFilePicker());
                        }

                        documentAdapter = new DocumentAdapter(documentList, this, this, isMentor);
                        documentsRecyclerView.setAdapter(documentAdapter);
                        loadDocuments();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Role check failed", e);
                        documentAdapter = new DocumentAdapter(documentList, this, this, false);
                        documentsRecyclerView.setAdapter(documentAdapter);
                        loadDocuments();
                    });
        } else {
            documentAdapter = new DocumentAdapter(documentList, this, this, false);
            documentsRecyclerView.setAdapter(documentAdapter);
            loadDocuments();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select Document"), PICK_FILE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                uploadFileToCloudinary(selectedFileUri);
            }
        }
    }

    private void uploadFileToCloudinary(Uri fileUri) {
        new Thread(() -> {
            try {
                String cloudName = "dycj9nypi";
                String apiKey    = "614464381627815";
                String apiSecret = "IxPlqSEmP_A6JyuP0ZgN4yoyciU";
                String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

                // 1) Read entire file
                InputStream is = requireActivity()
                        .getContentResolver()
                        .openInputStream(fileUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                byte[] fileBytes = baos.toByteArray();
                is.close();
                baos.close();

                // 2) Build signature
                String signature = sha1("timestamp=" + timestamp + apiSecret);

                // 3) Point at the raw endpoint
                URL url = new URL(
                        "https://api.cloudinary.com/v1_1/"
                                + cloudName
                                + "/raw/upload"
                );
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String boundary = "----" + System.currentTimeMillis();
                conn.setRequestProperty(
                        "Content-Type",
                        "multipart/form-data; boundary=" + boundary
                );

                DataOutputStream dos =
                        new DataOutputStream(conn.getOutputStream());
                // form fields
                writeFormField(dos, boundary, "api_key", apiKey);
                writeFormField(dos, boundary, "timestamp", timestamp);
                writeFormField(dos, boundary, "signature", signature);
                writeFormField(dos, boundary, "resource_type", "raw");
                // file field
                writeFileField(dos, boundary, "file",
                        "upload.pdf", fileBytes);

                dos.writeBytes("--" + boundary + "--\r\n");
                dos.flush();
                dos.close();

                // 4) Parse response
                InputStream resp = conn.getInputStream();
                String json = new BufferedReader(new InputStreamReader(resp))
                        .lines()
                        .collect(Collectors.joining("\n"));
                JSONObject o = new JSONObject(json);
                String fileUrl  = o.getString("secure_url");
                String publicId = o.getString("public_id");
                Log.d(TAG, "Upload complete. RAW URL: " + fileUrl);

                saveToFirestore(fileUrl, publicId);
            } catch (Exception e) {
                Log.e(TAG, "Upload failed", e);
            }
        }).start();
    }

    private void saveToFirestore(String fileUrl, String publicId) {
        if (currentUser == null) return;

        Log.d(TAG, "Saving fileUrl=" + fileUrl);  // this will be a raw/upload URL now

        String customTitle = documentTitleInput.getText().toString().trim();
        if (customTitle.isEmpty()) customTitle = "Untitled Document";

        Map<String, Object> docData = new HashMap<>();
        docData.put("title", customTitle);
        docData.put("mentorId", currentUser.getUid());
        docData.put("fileUrl", fileUrl);      // no replace needed
        docData.put("publicId", publicId);
        docData.put("timestamp", System.currentTimeMillis());

        db.collection("documents")
                .add(docData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Document uploaded and saved: " + documentReference.getId());
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Upload successful!", Toast.LENGTH_SHORT).show();
                        documentTitleInput.setText("");
                        loadDocuments();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save document metadata", e);
                    Toast.makeText(getContext(), "Failed to save document", Toast.LENGTH_SHORT).show();
                });
    }

    private void writeFormField(DataOutputStream dos, String boundary, String name, String value) throws Exception {
        dos.writeBytes("--" + boundary + "\r\n");
        dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        dos.writeBytes(value + "\r\n");
    }

    private void writeFileField(DataOutputStream dos, String boundary, String name, String fileName, byte[] data) throws Exception {
        dos.writeBytes("--" + boundary + "\r\n");
        dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n");
        dos.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
        dos.write(data);
        dos.writeBytes("\r\n");
    }

    private String sha1(String input) throws Exception {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void loadDocuments() {
        documentList.clear();

        db.collection("documents")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id      = doc.getId();
                        String title = doc.getString("title");
                        String fileUrl = doc.getString("fileUrl");

                        documentList.add(new Document(id, title, "", fileUrl));
                    }
                    documentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load documents from Firestore", e);
                    Toast.makeText(getContext(), "Failed to load documents", Toast.LENGTH_SHORT).show();
                });
    }



    //Download from URL Part

    @Override
    public void onDownloadClick(Document document) {
        String originalUrl = document.getDownloadUrl();  // e.g. …/image/upload/...pdf
        Log.d(TAG, "Download URL: " + originalUrl);

        if (originalUrl == null || originalUrl.isEmpty()) {

            Toast.makeText(getContext(), "No file to download", Toast.LENGTH_SHORT).show();
            return;
        }

        // On Android ≤ P (API <29), we need WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            pendingDocument = document;
            requestPermissions(
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    REQUEST_WRITE_STORAGE
            );
            return;
        }

        // Permission already granted (or Android 10+): start the download
        startDownload(originalUrl, document.getTitle());
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // retry download
            onDownloadClick(pendingDocument);
        } else {
            Toast.makeText(getContext(),
                    "Storage permission denied", Toast.LENGTH_SHORT).show();
        }
    }
    private void startDownload(String url, String title) {
        String fileName = title;
        if (fileName == null || fileName.isEmpty()) {
            fileName = Uri.parse(url).getLastPathSegment();
        }

        DownloadManager.Request request =
                new DownloadManager.Request(Uri.parse(url))
                        .setTitle("Downloading " + fileName)
                        .setDescription("Please wait…")
                        .setNotificationVisibility(
                                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        )
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)
                        .setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                fileName
                        );

        DownloadManager dm = (DownloadManager)
                requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm != null) {
            dm.enqueue(request);
            Toast.makeText(getContext(),
                    "Download started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(),
                    "Download manager unavailable", Toast.LENGTH_SHORT).show();
        }
    }
    //END Download from URL Part
    @Override
    public void onDeleteClick(Document document) {
        db.collection("documents")
                .document(document.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    documentList.remove(document);
                    documentAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(),
                            "Document deleted",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Delete failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }





}

