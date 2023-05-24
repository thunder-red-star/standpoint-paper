package me.thdr.standpoint.utils;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

class PerspectiveAPIRequest {
    private final String message;
    private final String apiKey;
    private final ArrayList<String> attributes;
    private final ArrayList<String> languages;
    private final boolean doNotStore;

    PerspectiveAPIRequest(String message, String apiKey, ArrayList<String> attributes, ArrayList<String> languages, boolean doNotStore) {
        // Save the message, API key, attributes, languages, and do not store flag.
        this.message = message;
        this.apiKey = apiKey;
        this.attributes = attributes;
        this.languages = languages;
        this.doNotStore = doNotStore;
    }

    private static String attributesToString(ArrayList<String> attributes) {
        // Convert arraylist to JSON string. Format: "attribute1": {}, "attribute2": {}, ...
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < attributes.size(); i++) {
            builder.append("\"");
            builder.append(attributes.get(i));
            builder.append("\": {}");
            if (i != attributes.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    // Send the request and return the response.
    public JsonObject sendRequest() throws IOException {
        // Create a new URL object.
        URL url = new URL("https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + this.apiKey);

        // Create a new HttpURLConnection object.
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the request method to POST.
        connection.setRequestMethod("POST");

        // Set the request headers.
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setDoOutput(true);

        // Build the body of the request. We will use a StringBuilder object.
        StringBuilder body = new StringBuilder();
        // Append the comment object (not the last key)
        body.append("{comment: {text: \"");
        body.append(this.message);
        body.append("\"},");
        // Append the languages array (not the last key)
        body.append("languages: [\"");
        body.append(this.languages.stream().reduce((a, b) -> a + "\", \"" + b).orElse(""));
        body.append("\"],");
        // Append the requested attributes object (not the last key)
        body.append("requestedAttributes: {");
        // Convert arraylist to JSON string. Format: "attribute1": {}, "attribute2": {}, ...
        body.append(attributesToString(this.attributes));
        body.append("},");
        // Append the do not store flag.
        body.append("doNotStore: ");
        body.append(this.doNotStore);
        // Append the last key.
        body.append("}");

        // Set the body of the request.
        connection.getOutputStream().write(body.toString().getBytes(StandardCharsets.UTF_8));

        // Create a new BufferedReader object.
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        // Create a new StringBuilder object.
        StringBuilder response = new StringBuilder();

        // Create a new String object.
        String line;

        // Loop through the response.
        while ((line = reader.readLine()) != null) {
            // Append the line to the response.
            response.append(line);
        }

        String responseString = response.toString();

        // Return the JSON object, parsed with Gson.
        return new Gson().fromJson(responseString, JsonObject.class);
    }
}

public class PerspectiveRequester {
    private final String apiKey;
    private final ArrayList<String> attributes;
    private final ArrayList<String> languages;
    private final boolean doNotStore;

    // Requester class. This class will be used to send requests to the Perspective API.
    public PerspectiveRequester(String apiKey, ArrayList<String> attributes, ArrayList<String> languages, boolean doNotStore) {
        this.apiKey = apiKey;
        this.attributes = attributes;
        this.languages = languages;
        this.doNotStore = doNotStore;
    }

    // Send a request to the Perspective API.
    public JsonObject sendRequest(String message) throws IOException {
        // Create a new PerspectiveAPIRequest object.
        PerspectiveAPIRequest request = new PerspectiveAPIRequest(message, this.apiKey, this.attributes, this.languages, this.doNotStore);

        // Try to send the request.
        try {
            // Return the response.
            return request.sendRequest();
        } catch (IOException e) {
            // Print the stack trace.
            e.printStackTrace();

            // Return null.
            return null;
        }
    }
}