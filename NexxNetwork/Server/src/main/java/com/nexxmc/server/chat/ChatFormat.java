package com.nexxmc.server.chat;

public class ChatFormat {

    private final String permission;
    private final String rankColour;
    private final String nameColour;
    private final String chatColour;

    public ChatFormat(String permission, String rankColour, String nameColour, String chatColour) {
        this.permission = permission;
        this.rankColour = rankColour;
        this.nameColour = nameColour;
        this.chatColour = chatColour;
    }

    public String getPermission() {
        return permission;
    }

    public String getRankColour() {
        return rankColour;
    }

    public String getNameColour() {
        return nameColour;
    }

    public String getChatColour() {
        return chatColour;
    }
}
