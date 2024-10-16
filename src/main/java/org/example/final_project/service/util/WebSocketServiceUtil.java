package org.example.final_project.service.util;

import org.springframework.web.socket.WebSocketSession;

public class WebSocketServiceUtil {

    public static String removeSubstring(String original, int startIndex, int length, int offset) {
        // Check for valid index and length
        if (startIndex < 0) {
            startIndex -= offset;
        }

        // Concatenate the part before and after the substring to be removed
        return original.substring(0, startIndex) + original.substring(startIndex + length);
    }

    public static String extractFileId(WebSocketSession session) {
        return session.getUri().getPath().split("/")[3];  // Extract fileId from the WebSocket URL
    }
}
