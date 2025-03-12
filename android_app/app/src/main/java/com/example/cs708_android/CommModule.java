package com.example.cs708_android;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommModule {
    private final static String SERVER_IP = "http://192.168.10.126:5000/";
    private final static String TAG = "CommModule";

    // Max chunk size in bytes (100KB)
    private final static int MAX_CHUNK_SIZE = 100 * 1024;

    /**
     * Generate a unique session ID to associate multiple API calls together
     * @return String unique session ID
     */
    public static String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Main method that handles the sequential upload of all data
     * This method coordinates multiple API calls but presents a simple interface to the caller
     */
    public static void callTargetDetectionApi(String command, File sceneImageFile, String depth_image,
                                              String translate_matrix, String camera_matrix,
                                              final MainActivity sourceActivity) {
        // Log data sizes for monitoring
        int image_size_kb = (int)(sceneImageFile.length() / 1024);
        int depth_size_kb = (depth_image != null) ? (depth_image.length() / 1024) : 0;
        int translate_size_kb = (translate_matrix != null) ? (translate_matrix.length() / 1024) : 0;
        int camera_size_kb = (camera_matrix != null) ? (camera_matrix.length() / 1024) : 0;
        int total_size_kb = image_size_kb + depth_size_kb + translate_size_kb + camera_size_kb;

        Log.d(TAG, "Image file size: " + image_size_kb + " KB");
        Log.d(TAG, "Depth data size: " + depth_size_kb + " KB");
        Log.d(TAG, "Translation matrix size: " + translate_size_kb + " KB");
        Log.d(TAG, "Camera matrix size: " + camera_size_kb + " KB");
        Log.d(TAG, "Total data size: " + total_size_kb + " KB (" + (total_size_kb / 1024.0) + " MB)");

        // Store upload session data
        final UploadSession session = new UploadSession();
        session.sessionId = generateSessionId();
        session.command = command;
        session.sceneImageFile = sceneImageFile;
        session.depthImage = depth_image;
        session.translateMatrix = translate_matrix;
        session.cameraMatrix = camera_matrix;
        session.sourceActivity = sourceActivity;

        Log.d(TAG, "Starting sequential upload with session ID: " + session.sessionId);

        // Start the upload sequence with the scene image
        uploadSceneImage(session);
    }

    /**
     * Upload scene image file to the server in chunks
     * @param session Upload session containing all data and state
     */
    private static void uploadSceneImage(final UploadSession session) {
        if (session.sceneImageFile == null || !session.sceneImageFile.exists()) {
            Log.e(TAG, "Scene image file is null or doesn't exist");
            return;
        }

        int fileSizeKb = (int)(session.sceneImageFile.length() / 1024);
        Log.d(TAG, "Image file size: " + fileSizeKb + " KB");

        try {
            // Read the file into a byte array
            byte[] fileBytes = new byte[(int) session.sceneImageFile.length()];
            java.io.FileInputStream fis = new java.io.FileInputStream(session.sceneImageFile);
            fis.read(fileBytes);
            fis.close();

            // Convert to Base64 for text-based chunking
            String base64Image = android.util.Base64.encodeToString(fileBytes, android.util.Base64.DEFAULT);
            Log.d(TAG, "Converted image to Base64 string, length: " + base64Image.length());

            // Prepare chunks
            List<String> chunks = prepareChunks(base64Image);
            int totalChunks = chunks.size();
            Log.d(TAG, "Splitting scene image into " + totalChunks + " chunks of max 100KB each");

            // Upload all chunks sequentially
            uploadChunksSequentially(
                    SERVER_IP + "api/scene_image_chunk",
                    session.sessionId,
                    chunks,
                    "scene_image",
                    new ChunkUploadListener() {
                        @Override
                        public void onComplete(String response) {
                            Log.d(TAG, "Step 1 complete: Scene image chunks uploaded");
                            // Move to the next step
                            uploadDepthImage(session);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e(TAG, "Error uploading scene image chunks: " + errorMessage);
                            // Try to continue to next step even if this one failed
                            uploadDepthImage(session);
                        }
                    }
            );
        } catch (java.io.IOException e) {
            Log.e(TAG, "Error reading scene image file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Upload depth image data to the server in chunks
     * @param session Upload session containing all data and state
     */
    private static void uploadDepthImage(final UploadSession session) {
        if (session.depthImage == null || session.depthImage.isEmpty()) {
            Log.e(TAG, "Depth image data is null or empty");
            // Continue to next step even if this data is missing
            uploadTranslateMatrix(session);
            return;
        }

        int dataSizeKb = session.depthImage.length() / 1024;
        Log.d(TAG, "Depth data size: " + dataSizeKb + " KB");

        // Prepare chunks
        List<String> chunks = prepareChunks(session.depthImage);
        int totalChunks = chunks.size();
        Log.d(TAG, "Splitting depth data into " + totalChunks + " chunks of max 100KB each");

        // Upload all chunks sequentially
        uploadChunksSequentially(
                SERVER_IP + "api/depth_data_chunk",
                session.sessionId,
                chunks,
                "depth_image",
                new ChunkUploadListener() {
                    @Override
                    public void onComplete(String response) {
                        Log.d(TAG, "Step 2 complete: Depth image chunks uploaded");
                        // Move to the next step
                        uploadTranslateMatrix(session);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error uploading depth image chunks: " + errorMessage);
                        // Continue to next step even if this one failed
                        uploadTranslateMatrix(session);
                    }
                }
        );
    }

    /**
     * Upload translation matrix to the server in chunks
     * @param session Upload session containing all data and state
     */
    private static void uploadTranslateMatrix(final UploadSession session) {
        if (session.translateMatrix == null || session.translateMatrix.isEmpty()) {
            Log.e(TAG, "Translation matrix is null or empty");
            // Continue to next step even if this data is missing
            uploadCameraMatrix(session);
            return;
        }

        int dataSizeKb = session.translateMatrix.length() / 1024;
        Log.d(TAG, "Translation matrix size: " + dataSizeKb + " KB");

        // Prepare chunks
        List<String> chunks = prepareChunks(session.translateMatrix);
        int totalChunks = chunks.size();
        Log.d(TAG, "Splitting translation matrix into " + totalChunks + " chunks of max 100KB each");

        // Upload all chunks sequentially
        uploadChunksSequentially(
                SERVER_IP + "api/translation_matrix_chunk",
                session.sessionId,
                chunks,
                "translate_matrix",
                new ChunkUploadListener() {
                    @Override
                    public void onComplete(String response) {
                        Log.d(TAG, "Step 3 complete: Translation matrix chunks uploaded");
                        // Move to the next step
                        uploadCameraMatrix(session);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error uploading translation matrix chunks: " + errorMessage);
                        // Continue to next step even if this one failed
                        uploadCameraMatrix(session);
                    }
                }
        );
    }

    /**
     * Upload camera matrix to the server in chunks
     * @param session Upload session containing all data and state
     */
    private static void uploadCameraMatrix(final UploadSession session) {
        if (session.cameraMatrix == null || session.cameraMatrix.isEmpty()) {
            Log.e(TAG, "Camera matrix is null or empty");
            // Continue to next step even if this data is missing
            sendCommand(session);
            return;
        }

        int dataSizeKb = session.cameraMatrix.length() / 1024;
        Log.d(TAG, "Camera matrix size: " + dataSizeKb + " KB");

        // Prepare chunks
        List<String> chunks = prepareChunks(session.cameraMatrix);
        int totalChunks = chunks.size();
        Log.d(TAG, "Splitting camera matrix into " + totalChunks + " chunks of max 100KB each");

        // Upload all chunks sequentially
        uploadChunksSequentially(
                SERVER_IP + "api/camera_matrix_chunk",
                session.sessionId,
                chunks,
                "camera_matrix",
                new ChunkUploadListener() {
                    @Override
                    public void onComplete(String response) {
                        Log.d(TAG, "Step 4 complete: Camera matrix chunks uploaded");
                        // Move to the next step
                        sendCommand(session);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error uploading camera matrix chunks: " + errorMessage);
                        // Continue to next step even if this one failed
                        sendCommand(session);
                    }
                }
        );
    }

    /**
     * Send command to the server and proceed to final processing
     * @param session Upload session containing all data and state
     */
    private static void sendCommand(final UploadSession session) {
        if (session.command == null || session.command.isEmpty()) {
            Log.e(TAG, "Command is null or empty");
            return;
        }

        // Using a specific endpoint for commands
        String url = SERVER_IP + "api/command";
        Log.d(TAG, "Sending command to " + url);

        AndroidNetworking.post(url)
                .setPriority(Priority.HIGH)
                .addBodyParameter("command", session.command)
                .addBodyParameter("session_id", session.sessionId)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Step 5 complete: Command sent");
                        // Move to final step: target detection
                        processTarget(session);
                    }

                    @Override
                    public void onError(ANError error) {
                        logError(error);
                        Log.e(TAG, "Error sending command: " + error.getErrorDetail());
                    }
                });
    }

    /**
     * Process target detection (final step)
     * @param session Upload session containing all data and state
     */
    private static void processTarget(final UploadSession session) {
        Log.d(TAG, "Starting final step: Target detection");

        AndroidNetworking.post(SERVER_IP + "api/process_target")
                .setPriority(Priority.IMMEDIATE)
                .addBodyParameter("session_id", session.sessionId)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Target detection complete");
                        Log.d(TAG, "Target detection result: " + response);
                        // Pass response to the activity
                        session.sourceActivity.selectTargetScreenArea(response);
                        // Clean up
                        session.sceneImageFile.delete();
                    }

                    @Override
                    public void onError(ANError error) {
                        logError(error);
                        Log.e(TAG, "Error in final step: " + error.getErrorDetail());
                    }
                });
    }

    /**
     * Helper method to split a large string into chunks
     * @param data String data to split
     * @return List of chunks
     */
    private static List<String> prepareChunks(String data) {
        List<String> chunks = new ArrayList<>();
        int dataLength = data.length();
        int chunksCount = (int) Math.ceil((double) dataLength / MAX_CHUNK_SIZE);

        for (int i = 0; i < chunksCount; i++) {
            int startIndex = i * MAX_CHUNK_SIZE;
            int endIndex = Math.min(startIndex + MAX_CHUNK_SIZE, dataLength);
            chunks.add(data.substring(startIndex, endIndex));
        }

        return chunks;
    }

    /**
     * Upload chunks sequentially using a non-recursive approach
     * @param url API endpoint to call
     * @param sessionId Unique session ID
     * @param chunks List of data chunks to upload
     * @param dataType Type of data for finalization
     * @param listener Callback for completion
     */
    private static void uploadChunksSequentially(final String url, final String sessionId,
                                                 final List<String> chunks, final String dataType,
                                                 final ChunkUploadListener listener) {
        // Start with the first chunk
        uploadNextChunk(url, sessionId, chunks, dataType, 0, listener);
    }

    /**
     * Upload the next chunk in the sequence
     */
    private static void uploadNextChunk(final String url, final String sessionId, final List<String> chunks,
                                        final String dataType, final int currentChunkIndex,
                                        final ChunkUploadListener listener) {
        int totalChunks = chunks.size();

        // Check if we're done with all chunks
        if (currentChunkIndex >= totalChunks) {
            finalizeChunkedUpload(sessionId, dataType, listener);
            return;
        }

        String chunk = chunks.get(currentChunkIndex);
        Log.d(TAG, "Uploading " + dataType + " chunk " + (currentChunkIndex + 1) + "/" + totalChunks +
                " (size: " + chunk.length() + " bytes)");

        AndroidNetworking.post(url)
                .setPriority(Priority.HIGH)
                .addBodyParameter("chunk_data", chunk)
                .addBodyParameter("chunk_index", String.valueOf(currentChunkIndex))
                .addBodyParameter("total_chunks", String.valueOf(totalChunks))
                .addBodyParameter("session_id", sessionId)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        // Calculate and report progress
                        int progress = (int)((currentChunkIndex + 1) * 100.0 / totalChunks);
                        Log.d(TAG, dataType + " chunk " + (currentChunkIndex + 1) +
                                " uploaded. Progress: " + progress + "%");

                        // Process the next chunk (non-recursive)
                        uploadNextChunk(url, sessionId, chunks, dataType, currentChunkIndex + 1, listener);
                    }

                    @Override
                    public void onError(ANError error) {
                        logError(error);
                        if (listener != null) {
                            listener.onError("Error uploading " + dataType + " chunk " +
                                    (currentChunkIndex + 1) + ": " + error.getErrorDetail());
                        }
                    }
                });
    }

    /**
     * Helper method to finalize a chunked upload
     */
    private static void finalizeChunkedUpload(String sessionId, String dataType,
                                              final ChunkUploadListener listener) {
        String url = SERVER_IP + "api/finalize_chunked_upload";
        Log.d(TAG, "Finalizing chunked upload for " + dataType);

        AndroidNetworking.post(url)
                .setPriority(Priority.HIGH)
                .addBodyParameter("session_id", sessionId)
                .addBodyParameter("data_type", dataType)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Finalization response: " + response);
                        if (listener != null) {
                            listener.onComplete(response);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        logError(error);
                        if (listener != null) {
                            listener.onError("Error finalizing chunked upload: " + error.getErrorDetail());
                        }
                    }
                });
    }

    /**
     * Helper method to log error details
     */
    private static void logError(ANError error) {
        Log.e(TAG, "Error: " + error.getErrorDetail());
        Log.e(TAG, "Error code: " + error.getErrorCode());
        Log.e(TAG, "Error body: " + error.getErrorBody());
    }

    /**
     * Interface for chunk upload callbacks
     */
    private interface ChunkUploadListener {
        void onComplete(String response);
        void onError(String errorMessage);
    }

    /**
     * Class to hold all data for an upload session
     */
    private static class UploadSession {
        String sessionId;
        String command;
        File sceneImageFile;
        String depthImage;
        String translateMatrix;
        String cameraMatrix;
        MainActivity sourceActivity;
    }
}