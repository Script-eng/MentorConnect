// Create this in a suitable package, e.g., hu.nje.mentorconnect.receivers
package hu.nje.mentorconnect.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import hu.nje.mentorconnect.R;

public class DownloadCompletionReceiver extends BroadcastReceiver {

    private static final String TAG = "DownloadReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || !DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            return;
        }

        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        if (downloadId == -1) {
            Log.e(TAG, "Received download complete intent with no ID.");
            return;
        }

        Log.d(TAG, "Download complete intent received for ID: " + downloadId);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) {
            Log.e(TAG, "DownloadManager service not available.");
            return;
        }

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        Cursor cursor = null;
        try {
            cursor = downloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int titleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
                int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI); // URI where the file is stored

                int status = cursor.getInt(statusIndex);
                String title = cursor.getString(titleIndex);
                if (title == null || title.isEmpty()) {
                    title = "Downloaded File"; // Fallback title
                }

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    String fileUriString = cursor.getString(uriIndex);
                    Uri fileUri = (fileUriString != null) ? Uri.parse(fileUriString) : null;

                    Log.d(TAG, "Download successful for '" + title + "'. URI: " + fileUriString);
                    String message = context.getString(R.string.download_complete_toast, title);

                    // Show a Snackbar or Toast with an action to open the file
                    showCompletionSnackbar(context, message, fileUri, downloadManager, downloadId);

                } else if (status == DownloadManager.STATUS_FAILED) {
                    int reason = cursor.getInt(reasonIndex);
                    String reasonText = getDownloadErrorReason(context, reason);
                    Log.e(TAG, "Download failed for '" + title + "'. Reason: " + reason + " (" + reasonText + ")");
                    Toast.makeText(context, context.getString(R.string.download_failed_status_toast, title) + ": " + reasonText, Toast.LENGTH_LONG).show();

                } else {
                    Log.d(TAG, "Download status for '" + title + "': " + status);
                    // Handle other statuses if needed (PAUSED, PENDING, RUNNING)
                }
            } else {
                Log.w(TAG, "DownloadManager query returned no results for ID: " + downloadId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying DownloadManager for ID: " + downloadId, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Display Snackbar with an "Open" action
    private void showCompletionSnackbar(Context context, String message, Uri fileUri, DownloadManager dm, long downloadId) {
        // We need a View to anchor the Snackbar. This is tricky from a BroadcastReceiver.
        // A simple Toast is more reliable here unless we pass view information or use events.
        // Let's stick to Toast for simplicity from the receiver.
        // To use Snackbar, the fragment/activity would need to register the receiver dynamically
        // and have access to its own view hierarchy.

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        // Optional: Directly try to open if URI is valid
        if (fileUri != null) {
            Log.d(TAG,"Attempting to automatically suggest opening file: " + fileUri);
            // This might be too intrusive. Let's keep it simple with just the Toast for now.
            // If needed, one could implement a notification click handler instead.
            // openFile(context, fileUri, dm, downloadId);
        }
    }


    // Helper method to open the downloaded file
    private void openFile(Context context, Uri fileUri, DownloadManager downloadManager, long downloadId) {
        if (fileUri == null) return;

        String mimeType = downloadManager.getMimeTypeForDownloadedFile(downloadId);
        if (mimeType == null) {
            // Fallback if mime type isn't correctly determined by DownloadManager
            // Could try to infer from file extension if needed, but often unnecessary
            mimeType = "*/*";
            Log.w(TAG, "Could not get MIME type from DownloadManager for ID " + downloadId + ", using */*");
        }

        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(fileUri, mimeType);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Important for accessing the file

        try {
            context.startActivity(openIntent);
        } catch (android.content.ActivityNotFoundException e) {
            Log.e(TAG, "No application found to open file: " + fileUri + " with MIME type: " + mimeType, e);
            Toast.makeText(context, R.string.error_opening_file, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error trying to open file: " + fileUri, e);
            Toast.makeText(context, context.getString(R.string.error_opening_file) + " (Error)", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper to get human-readable error reason
    private String getDownloadErrorReason(Context context, int reason) {
        String reasonText;
        switch (reason) {
            case DownloadManager.ERROR_CANNOT_RESUME: reasonText = "Cannot Resume"; break;
            case DownloadManager.ERROR_DEVICE_NOT_FOUND: reasonText = "Device Not Found"; break;
            case DownloadManager.ERROR_FILE_ALREADY_EXISTS: reasonText = "File Already Exists"; break;
            case DownloadManager.ERROR_FILE_ERROR: reasonText = "File Error"; break;
            case DownloadManager.ERROR_HTTP_DATA_ERROR: reasonText = "HTTP Data Error"; break;
            case DownloadManager.ERROR_INSUFFICIENT_SPACE: reasonText = "Insufficient Space"; break;
            case DownloadManager.ERROR_TOO_MANY_REDIRECTS: reasonText = "Too Many Redirects"; break;
            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE: reasonText = "Unhandled HTTP Code"; break;
            case DownloadManager.ERROR_UNKNOWN: reasonText = "Unknown Error"; break;
            default: reasonText = "Error Code: " + reason; break;
        }
        return reasonText;
    }
}
