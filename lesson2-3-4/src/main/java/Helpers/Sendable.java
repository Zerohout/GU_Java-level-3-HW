package Helpers;

import Message.Message;

public interface Sendable {
    String getName();
    void sendMessage(Message msg);
    void sendLocalMessage(Message msg);
}
