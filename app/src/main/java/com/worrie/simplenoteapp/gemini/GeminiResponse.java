package com.worrie.simplenoteapp.gemini;

import java.util.List;

// This class represents the JSON structure we get back FROM the Gemini API.
// It's designed to help Gson parse the response we receive.
public class GeminiResponse {

    // The API response contains a list of "candidates" or possible answers.
    private List<GeminiCandidate> candidates;

    // Getter for Gson to access the list of candidates.
    public List<GeminiCandidate> getCandidates() {
        return candidates;
    }

    // This is a nested class representing one of the "candidates" in the response.
    // By making it and its methods public, other parts of the app (like MainActivity)
    // can access the data inside it.
    public static class GeminiCandidate {
        private GeminiContent content;

        public GeminiContent getContent() {
            return content;
        }
    }
}
