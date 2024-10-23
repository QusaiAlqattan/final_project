package org.example.final_project.model;

public class Message {
    private String value;
    private String type;
    private int length;
    private int position;
    private boolean fullContent;

    public Message(String value, String type, int length, int position, boolean isFullContent) {
        this.value = value;
        this.type = type;
        this.length = length;
        this.position = position;
        this.fullContent = isFullContent;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isFullContent() {
        return fullContent;
    }

    public void setFullContent(boolean fullContent) {
        this.fullContent = fullContent;
    }
}
