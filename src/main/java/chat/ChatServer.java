package chat;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import chat.database.DatabaseManager;
import chat.info.*;
import chat.utils.*;

public class ChatServer extends UnicastRemoteObject implements IChatServer {
    private final Map<String, UserInfo> users = new HashMap<>(); // username -> UserInfo
    private final Map<String, IChatClient> onlineUsers = new HashMap<>(); // username -> client instance
    private final Map<String, GroupInfo> groups = new HashMap<>(); // Mapeia o nome do grupo para o objeto Group
    private Map<String, List<MessageInfo>> messageHistory = new ConcurrentHashMap<>();
    private final DatabaseManager dbManager = new DatabaseManager();
    private Map<String, IChatClient> clientMap = new HashMap<>();

    private Map<String, FileInfo> storedFiles = new ConcurrentHashMap<>();

    public ChatServer() throws RemoteException {
        super();
        // Usuários iniciais com nome de usuário, senha e e-mail
        users.put("user1", new UserInfo("user1", HashUtil.generateHash("password1"), "user1@mail.com"));
        users.put("user2", new UserInfo("user2", HashUtil.generateHash("password2"), "user2@mail.com"));
    }

    // @Override
    // public boolean registerUser(String username, String password, String email)
    // throws RemoteException {
    // if (users.containsKey(username)) {
    // return false; // Usuário já existe
    // }

    // users.put(username, new UserInfo(username, HashUtil.generateHash(password),
    // email));
    // return true; // Usuário registrado com sucesso
    // }

    // @Override
    // public boolean login(String username, String password, IChatClient client)
    // throws RemoteException {

    // if (users.containsKey(username)) {
    // String storedHash = users.get(username).getPassword();
    // if (storedHash.equals(HashUtil.generateHash(password))) {
    // onlineUsers.put(username, client);
    // System.out.println(username + " logged in.");
    // return true;
    // }

    // }
    // return false;
    // }

    @Override
    public boolean registerUser(String username, String password, String email) throws RemoteException {
        return dbManager.registerUser(username, password, email);
    }

    @Override
    public boolean login(String username, String password, IChatClient client) throws RemoteException {
        if (dbManager.login(username, password)) {
            onlineUsers.put(username, client);
            System.out.println(username + " logged in.");
            return true;
        }
        return false;
    }

    @Override
    public void logout(String username) throws RemoteException {
        onlineUsers.remove(username);
        System.out.println(username + " logged out.");
    }

    // @Override
    // public List<String> listUsers() throws RemoteException {
    // return new ArrayList<>(users.keySet());
    // }

    @Override
    public List<String> listOnlineUsers() throws RemoteException {
        return new ArrayList<>(onlineUsers.keySet());
    }

    @Override
    public List<String> listUsers() throws RemoteException {
        return dbManager.listUsers();
    }

    // @Override
    // public void sendMessage(String sender, String recipient, String message)
    // throws RemoteException {
    // MessageInfo msg = new MessageInfo(sender, recipient, message);
    // storeMessage(sender, recipient, msg);
    // }

    // @Override
    // public void sendFile(String sender, String recipient, FileInfo file) throws
    // RemoteException {
    // MessageInfo msg = new MessageInfo(sender, recipient, file);
    // storeMessage(sender, recipient, msg);
    // }

    // private void storeMessage(String sender, String recipient, MessageInfo msg) {
    // String key = getChatKey(sender, recipient);
    // messageHistory.putIfAbsent(key, new ArrayList<>());
    // messageHistory.get(key).add(msg);
    // }

    // @Override
    // public List<MessageInfo> getMessageHistory(String user1, String user2) throws
    // RemoteException {
    // return messageHistory.getOrDefault(getChatKey(user1, user2), new
    // ArrayList<>());
    // }

    private String getChatKey(String user1, String user2) {
        List<String> users = Arrays.asList(user1, user2);
        Collections.sort(users); // Garante sempre a mesma ordem
        return users.get(0) + "_" + users.get(1);
    }

    // @Override
    // public FileInfo receiveFile(String sender, String recipient, String fileName)
    // throws RemoteException {
    // String key = getChatKey(sender, recipient);

    // List<MessageInfo> messages = messageHistory.getOrDefault(key, new
    // ArrayList<>());

    // for (Map.Entry<String, List<MessageInfo>> entry : messageHistory.entrySet())
    // {
    // System.out.println("Key: " + entry.getKey());
    // }

    // for (MessageInfo msg : messages) {

    // if (msg.isFile() && msg.getFile().getFileName().equals(fileName)) {
    // return msg.getFile(); // Retorna o arquivo encontrado
    // }
    // }
    // return null; // Retorna null se o arquivo não existir no histórico
    // }

    @Override
    public void sendMessage(String sender, String recipient, String message) throws RemoteException {
        long timestamp = System.currentTimeMillis();
        MessageInfo msg = new MessageInfo(sender, recipient, message, timestamp);
        dbManager.storeMessage(msg); // Agora armazenamos no banco de dados

        IChatClient recipientClient = getClientByUsername(recipient); // Você deve implementar isso
        if (recipientClient != null) {
            recipientClient.notifyNewMessage(sender); // Chama o callback no cliente
        }
    }

    @Override
    public void sendFile(String sender, String recipient, FileInfo file) throws RemoteException {
        long timestamp = System.currentTimeMillis();
        MessageInfo msg = new MessageInfo(sender, recipient, file, timestamp);
        dbManager.storeMessage(msg); // Também armazenamos arquivos no banco

        IChatClient recipientClient = getClientByUsername(recipient); // Você deve implementar isso
        if (recipientClient != null) {
            recipientClient.notifyNewMessage(sender); // Chama o callback no cliente
        }
    }

    @Override
    public List<MessageInfo> getMessageHistory(String user1, String user2) throws RemoteException {
        return dbManager.getMessageHistory(user1, user2); // Busca o histórico no banco de dados
    }

    @Override
    public FileInfo receiveFile(String sender, String recipient, String fileName) throws RemoteException {
        return dbManager.receiveFile(sender, recipient, fileName); // Busca um arquivo específico no banco
    }

    @Override
    public boolean createGroup(String groupName, String description, String owner) throws RemoteException {
        return dbManager.createGroup(groupName, description, owner);
    }

    @Override
    public boolean requestJoinGroup(String groupName, String username) throws RemoteException {
        return dbManager.requestJoinGroup(groupName, username);
    }

    @Override
    public boolean approveJoinRequest(String groupName, String owner, String username, boolean approve)
            throws RemoteException {
        boolean result = dbManager.approveJoinRequest(groupName, owner, username, approve);
        if (result) {
            IChatClient memberClient = getClientByUsername(username);
            if (memberClient != null) {
                memberClient.notifyGroupJoinApproval(groupName, approve);
            }
        }

        return result;
    }

    @Override
    public List<String> getPendingRequests(String groupName) throws RemoteException {
        return dbManager.getPendingRequests(groupName);
    }

    @Override
    public List<String> listGroups() throws RemoteException {
        return dbManager.listGroups();
    }

    @Override
    public void sendGroupMessage(String groupName, String sender, String message) throws RemoteException {
        dbManager.sendGroupMessage(groupName, sender, message);
        System.out.println("Mensagem: " + message);
        GroupInfo group = dbManager.getGroupInfo(groupName);
        if (group != null) {
            // Notifica todos os membros do grupo sobre a nova mensagem
            for (String member : group.getMembers()) {
                IChatClient memberClient = getClientByUsername(member);
                if (memberClient != null) {
                    memberClient.notifyNewMessage(groupName); // Chama o callback no cliente
                }
            }
        }
    }

    @Override
    public List<String> getGroupMessages(String groupName) throws RemoteException {
        return dbManager.getGroupMessages(groupName);
    }

    @Override
    public boolean leaveGroup(String groupName, String username) throws RemoteException {
        return dbManager.removeGroupMember(groupName, username);
    }

    @Override
    public boolean deleteGroup(String groupName, String requestingUser) throws RemoteException {
        GroupInfo group = dbManager.getGroupInfo(groupName);
        if (group == null)
            return false;

        // Permite se for o dono OU se for admin
        if (group.getOwner().equals(requestingUser) || isAdmin(requestingUser)) {
            boolean deleted = dbManager.deleteGroup(groupName);
            if (deleted) {
                System.out.println("✅ Grupo '" + groupName + "' deletado por " + requestingUser);
            }
            return deleted;
        } else {
            System.out.println("❌ Tentativa de deletar grupo '" + groupName + "' negada para " + requestingUser);
            return false;
        }
    }

    @Override
    public boolean removeUserFromGroup(String groupName, String userToRemove) throws RemoteException {
        boolean removed = dbManager.removeGroupMember(groupName, userToRemove);
        if (removed) {
            IChatClient memberClient = getClientByUsername(userToRemove);
            if (memberClient != null) {
                memberClient.notifyGroupRemoval(groupName);
            }
        }

        return removed;
    }

    @Override
    public GroupInfo getGroupInfo(String groupName) throws RemoteException {
        return dbManager.getGroupInfo(groupName);
    }

    @Override
    public boolean changeGroupOwner(String groupName, String newOwner) throws RemoteException {
        boolean changed = dbManager.changeGroupOwner(groupName, newOwner);
        IChatClient memberClient = getClientByUsername(newOwner);
        if (changed) {
            if (memberClient != null) {
                memberClient.notifyNewGroupOwner(groupName);
            }
        }

        return changed;
    }

    // Registra o cliente quando ele se conecta
    public void registerClient(String username, IChatClient client) throws RemoteException {
        clientMap.put(username, client);
    }

    // Remove o cliente quando ele se desconecta
    public void unregisterClient(String username) throws RemoteException {
        clientMap.remove(username);
    }

    private IChatClient getClientByUsername(String username) {
        return clientMap.get(username);
    }

    @Override
    public boolean deleteUser(String targetUsername, String requestingUsername) throws RemoteException {
        // Verifica permissões
        if (!targetUsername.equals(requestingUsername) && !isAdmin(requestingUsername)) {
            System.out.println("❌ Acesso negado: " + requestingUsername + " tentou deletar " + targetUsername);
            return false;
        }

        // Primeiro desloga o usuário alvo se estiver online
        logout(targetUsername);
        unregisterClient(targetUsername);

        // Remove do banco de dados (cascade manual)
        boolean deleted = dbManager.deleteUser(targetUsername);
        if (deleted) {
            System.out.println("✅ Usuário deletado: " + targetUsername + " (por " + requestingUsername + ")");
        }
        return deleted;
    }

    @Override
    public boolean isAdmin(String username) throws RemoteException {
        return dbManager.isAdmin(username);
    }
}
