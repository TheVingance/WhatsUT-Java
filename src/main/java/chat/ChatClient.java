package chat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.UI.ChatUI;
import chat.info.FileInfo;
import javafx.application.Platform;
import javafx.stage.FileChooser;

public class ChatClient extends UnicastRemoteObject implements IChatClient {
    private final String username;
    private final IChatServer server;
    private ChatUI ui;
    private Map<String, List<String>> unreadMessages = new HashMap<>();

    public ChatClient(String username, IChatServer server, ChatUI ui) throws RemoteException {
        super(); // O argumento 'this' é o objeto Callback sendo enviado
                 // return server.login(username, password, this);
        this.username = username;
        this.server = server;
        this.ui = ui;
    }

    @Override
    public boolean login(String password) throws RemoteException {
        return server.login(username, password, this);
    }

    @Override
    public void logout() throws RemoteException {
        server.logout(username);
        server.unregisterClient(username);
    }

    @Override
    public void sendMessage(String recipient, String message) throws RemoteException {
        server.sendMessage(username, recipient, message);
    }

    @Override
    public java.util.List<String> getUserList() throws RemoteException {
        return server.listOnlineUsers();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public List<String> getUnreadMessages(String sender) {
        // Retorna as mensagens não lidas do remetente, ou uma lista vazia se não houver
        return unreadMessages.getOrDefault(sender, new ArrayList<>());
    }

    @Override
    public void clearUnreadMessages(String sender) {
        // Remove as mensagens não lidas do remetente
        unreadMessages.remove(sender);
    }

    @Override
    public void receiveMessage(String sender, String message) throws RemoteException {
        Platform.runLater(() -> {
            if (ui != null) {
                // Verifica se a janela de chat com o remetente está aberta
                if (!ui.isChatWindowOpen(sender)) {
                    // Adiciona a mensagem à lista de não lidas
                    unreadMessages.computeIfAbsent(sender, k -> new ArrayList<>()).add(message);
                } else {
                    // Exibe a mensagem diretamente na janela de chat aberta
                    ui.displayReceivedMessage(sender, message);
                }
            } else {
                System.out.println(sender + " says: " + message);
            }
        });
    }

    @Override
    public void notifyNewMessage(String senderOrGroup) throws RemoteException {
        System.out.println("📨 Nova mensagem de " + senderOrGroup + ". Atualizando chat...");

        boolean isGroup = server.getGroupInfo(senderOrGroup) != null; // Verifica se é um grupo
        ui.updateChat(senderOrGroup, isGroup);
    }

    @Override
    public void notifyGroupJoinApproval(String groupName, boolean approved) throws RemoteException {
        String msg = approved ? "Sua solicitação para entrar no grupo '" + groupName + "' foi aprovada!"
                : "Sua solicitação para entrar no grupo '" + groupName + "' foi rejeitada.";
        System.out.println(msg);
        String content = "joinapproval";
        ui.showNotification(msg, content, "");
    }

    @Override
    public void notifyGroupRemoval(String groupName) throws RemoteException {
        String msg = "Você foi removido do grupo '" + groupName + "'.";
        System.out.println(msg);
        String content = "groupremoval";
        ui.showNotification(msg, content, groupName);
        // Você pode também atualizar a UI para remover o grupo da lista de chats, se
        // desejar.
    }

    @Override
    public void notifyNewGroupOwner(String groupName) throws RemoteException {
        String msg = "Você agora é o novo dono do grupo '" + groupName + "'.";
        System.out.println(msg);
        String content = "newowner";
        ui.showNotification(msg, content, groupName);
        // Se necessário, atualize a interface para exibir as opções de administração.
    }

    public boolean deleteUser(String password) throws RemoteException {
        // Implementação simplificada: autenticar com senha antes de deletar seria
        // ideal, mas a interface não pede senha no server.deleteUser.
        // Vamos assumir que se o cliente está logado, ele pode se deletar.
        // AUI pode pedir confirmação.
        return server.deleteUser(username, username);
    }

    public boolean deleteTargetUser(String targetUser) throws RemoteException {
        return server.deleteUser(targetUser, username);
    }

    public boolean isAdmin() throws RemoteException {
        return server.isAdmin(username);
    }

    public boolean deleteGroup(String groupName) throws RemoteException {
        return server.deleteGroup(groupName, username);
    }

}
