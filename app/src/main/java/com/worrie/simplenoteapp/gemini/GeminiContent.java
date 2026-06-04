package com.worrie.simplenoteapp.gemini;

import java.util.List;

// Represents a "content" object in both the request and the response.
public class GeminiContent {

    // A content object contains one or more "parts".
    private final List<GeminiPart> parts;

    public GeminiContent(List<GeminiPart> parts) {
        this.parts = parts;
    }

    // Getter for Gson.
    public List<GeminiPart> getParts() {
        return parts;
    }
}
