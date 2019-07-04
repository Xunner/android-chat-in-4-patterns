package nju.androidchat.client.hw2.viewmodel;

public interface UiOperator {
    void runOnUiThread(Runnable action);
    void scrollListToBottom();
}
