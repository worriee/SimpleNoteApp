package com.worrie.simplenoteapp;

public class Note {
    private String title;
    private String content;

    public Note(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        if (title != null && !title.isEmpty()) {
            return title;
        } else if (content != null && !content.isEmpty()) {
            return content;
        } else {
            return "New Note";
        }
    }
}
