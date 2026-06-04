package com.worrie.simplenoteapp.gemini;

import java.util.Collections;
import java.util.List;

// This class represents the JSON structure that we send TO the Gemini API.
// It's designed to match the format the API expects perfectly.
public class GeminiRequest {

    // The API expects a list of "contents". In our case, it's just one item.
    private final List<GeminiContent> contents;

    // This constructor makes it easy to create a request with just the prompt text.
    public GeminiRequest(String text) {
        // The structure is: Request -> Content -> Part -> text
        GeminiPart part = new GeminiPart(text);
        GeminiContent content = new GeminiContent(Collections.singletonList(part));
        this.contents = Collections.singletonList(content);
    }

    // Getter method required by the Gson library to create the JSON.
    public List<GeminiContent> getContents() {
        return contents;
    }
}
