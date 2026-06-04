package com.worrie.simplenoteapp.gemini;

// Represents a "part" object, which is the innermost piece of the request/response.
// It directly holds the text of our prompt.
public class GeminiPart {

    // The actual text content.
    private final String text;

    public GeminiPart(String text) {
        this.text = text;
    }

    // Getter for Gson.
    public String getText() {
        return text;
    }
}
