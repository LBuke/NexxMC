package com.nexxmc.server.chat;

import com.nexxmc.server.C;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.LinkedList;

public class ChatFormatListener implements Listener {

    private final LinkedList<ChatFormat> chatFormats;
    private final ChatFormat defaultFormat;

    public ChatFormatListener() {
        this.chatFormats = new LinkedList<ChatFormat>();
        this.chatFormats.add(new ChatFormat("rank.owner", C.DARK_RED, C.RED, C.WHITE));
        this.chatFormats.add(new ChatFormat("rank.admin", C.RED, C.RED, C.WHITE));

        this.defaultFormat = new ChatFormat("rank.default", C.WHITE, C.YELLOW, C.GRAY);
        this.chatFormats.add(this.defaultFormat);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        ChatFormat chatFormat = this.defaultFormat;
        for (ChatFormat format : this.chatFormats) {
            if (event.getPlayer().hasPermission(format.getPermission())) {
                chatFormat = format;
                break;
            }
        }


    }
}
