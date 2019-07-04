package nju.androidchat.client.mvvm0.viewmodel;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import nju.androidchat.client.BR;
import nju.androidchat.client.mvvm0.model.ClientMessageObservable;
import nju.androidchat.client.mvvm0.model.NumberInvisibleObservable;
import nju.androidchat.client.socket.MessageListener;
import nju.androidchat.client.socket.SocketClient;
import nju.androidchat.shared.message.ClientSendMessage;
import nju.androidchat.shared.message.ErrorMessage;
import nju.androidchat.shared.message.Message;
import nju.androidchat.shared.message.RecallMessage;
import nju.androidchat.shared.message.ServerSendMessage;

@Log
public class Mvvm0ViewModel extends BaseObservable implements MessageListener {
    @Bindable
    @Getter
    @Setter
    private NumberInvisibleObservable numberInvisible;

    @Bindable
    @Getter
    private String messageToSend;
    @Getter
    private ObservableList<ClientMessageObservable> messageObservableList;
    @Getter
    private SocketClient client;

    private UiOperator uiOperator;

    private String numberInvisibleToString(int n) {
        return " 未显示消息数量：" + n;
    }

    public void setMessageToSend(String messageToSend) {
        this.messageToSend = messageToSend;
        notifyPropertyChanged(BR.messageToSend);
    }

    public Mvvm0ViewModel(UiOperator uiOperator) {
        this.uiOperator = uiOperator;

        numberInvisible = new NumberInvisibleObservable(this.numberInvisibleToString(0));
        messageToSend = "";
        messageObservableList = new ObservableArrayList<>();
        client = SocketClient.getClient();
        client.setMessageListener(this);
        client.startListening();
    }

    private void updateList(ClientMessageObservable clientMessage) {
        this.updateNumInvisible();
        uiOperator.runOnUiThread(() -> {
            messageObservableList.add(clientMessage);
            uiOperator.scrollListToBottom();
        });
    }

    public void sendMessage() {
        LocalDateTime now = LocalDateTime.now();
        UUID uuid = UUID.randomUUID();
        String senderUsername = client.getUsername();
        ClientSendMessage clientSendMessage = new ClientSendMessage(uuid, now, messageToSend);
        ClientMessageObservable clientMessageObservable = new ClientMessageObservable(clientSendMessage, senderUsername);
        updateList(clientMessageObservable);

        AsyncTask.execute(() -> client.writeToServer(clientSendMessage));
    }

    @Override
    public void onMessageReceived(Message message) {
        if (message instanceof ServerSendMessage) {
            // 接受到其他设备发来的消息
            // 增加到自己的消息列表里，并通知UI修改
            ServerSendMessage serverSendMessage = (ServerSendMessage) message;
            log.info(String.format("%s sent a messageToSend: %s",
                    serverSendMessage.getSenderUsername(),
                    serverSendMessage.getMessage()
            ));
            ClientMessageObservable clientMessage = new ClientMessageObservable(serverSendMessage);
            updateList(clientMessage);
        } else if (message instanceof ErrorMessage) {
            // 接收到服务器的错误消息
            log.severe("Server error: " + ((ErrorMessage) message).getErrorMessage());

        } else if (message instanceof RecallMessage) {
            // 接受到服务器的撤回消息，MVVM-0不实现
        } else {
            // 不认识的消息
            log.severe("Unsupported messageToSend received: " + message.toString());

        }
    }

    public void disconnect() {
        AsyncTask.execute(() -> client.disconnect());
    }

    public void updateNumInvisible() {
//        int[] l = new int[2];
//        view.getLocationInWindow(l);
//        System.out.println(view.getLocalVisibleRect(new Rect()) + ", " + l[1] + ", " + view.getMeasuredHeight());
//        if (this.numberInvisible.getNumberInvisible().length() > 15)
//            numberInvisible.setNumberInvisible(numberInvisibleToString(1));
        numberInvisible.setNumberInvisible(this.numberInvisibleToString(Math.max(this.messageObservableList.size() - 9, 0)));
    }
}
