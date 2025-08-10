package psu.signlinguamobile.managers;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LocalHttpServer
{
    private static final String TAG = "LocalHttpServer";
    private final Context context;
    private final int port;
    private final AsyncHttpServer server;

    public LocalHttpServer(Context context, int port) {
        this.context = context;
        this.port = port;
        this.server = new AsyncHttpServer();
        setupRoutes();
    }

    private void setupRoutes() {
        // Catch-all GET route.
        server.get("^(.*)$", (request, response) -> {
            String uri = request.getPath();

            // Serve default pages for specific URIs.
            if (uri.equals("/") || uri.equals("/index.html")) {
                serveAsset("ml/index.html", "text/html", response);
                return;
            }

            // After handling specific URIs...
            // Remove the leading "/" if present.
            if (uri.startsWith("/")) {
                uri = uri.substring(1);
            }

            // If the requested asset doesn't start with "webml/", prepend it.
            if (!uri.startsWith("ml/")) {
                uri = "ml/" + uri;
            }

            serveAsset(uri, getMimeType(uri), response);
        });
    }

    /**
     * Determines the MIME type based on the file extension.
     */
    private String getMimeType(String uri) {
        if (uri.endsWith(".html"))
            return "text/html";
        if (uri.endsWith(".js") || uri.endsWith(".mjs"))
            return "application/javascript";
        if (uri.endsWith(".css"))
            return "text/css";
        if (uri.endsWith(".wasm"))
            return "application/wasm";
        if (uri.endsWith(".ts"))
            return "application/typescript";
        return "application/octet-stream";
    }

    /**
     * Loads the asset from the APK's assets folder and sends it as the HTTP response.
     * For text assets, the content is sent as a UTF-8 encoded String.
     * For binary assets, the content is sent as a raw byte stream.
     */
    private void serveAsset(String assetPath, String mimeType, AsyncHttpServerResponse response) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(assetPath);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            if (mimeType.startsWith("text/") ||
                    mimeType.equals("application/javascript") ||
                    mimeType.equals("application/typescript")) {
                String content = new String(buffer, StandardCharsets.UTF_8);
                // Using send(String, String) with an appended charset in the content type.
                response.send(mimeType + "; charset=UTF-8", content);
            } else {
                // Using send(String, byte[]) for binary assets.
                response.send(mimeType, buffer);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error serving asset: " + assetPath, e);
            response.code(404);
            response.send("404 Not Found");
        }
    }

    public void start() {
        server.listen(port);
    }

    public void stop() {

        if (server != null)
        {
            server.stop(); // Properly shuts down the local server
            // server = null; // Helps prevent memory leaks
            Log.d("LocalHttpServer", "Server stopped.");
        }
    }
}
