package chat;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import chat.info.FileInfo;
import chat.info.GroupInfo;
import chat.info.MessageInfo;

public interface IChatServer extends Remote {
    public boolean registerUser(String username, String password, String email) throws RemoteException;

    boolean login(String username, String password, IChatClient client) throws RemoteException;

    void logout(String username) throws RemoteException;

    List<String> listUsers() throws RemoteException; // Retorna todos os usuários

    List<String> listOnlineUsers() throws RemoteException; // Retorna apenas os usuários online

    public void sendMessage(String sender, String recipient, String message) throws RemoteException;

    public List<MessageInfo> getMessageHistory(String user1, String user2) throws RemoteException;

    public FileInfo receiveFile(String sender, String recipient, String fileName) throws RemoteException;

    public boolean createGroup(String groupName, String description, String owner) throws RemoteException;

    public boolean requestJoinGroup(String groupName, String username) throws RemoteException;

    public boolean approveJoinRequest(String groupName, String owner, String username, boolean approve)
            throws RemoteException;

    public List<String> listGroups() throws RemoteException;

    void sendGroupMessage(String groupName, String sender, String message) throws RemoteException;

    List<String> getGroupMessages(String groupName) throws RemoteException;

    boolean leaveGroup(String groupName, String username) throws RemoteException;

    GroupInfo getGroupInfo(String groupName) throws RemoteException;

    public boolean removeUserFromGroup(String groupName, String userToRemove) throws RemoteException;

    public boolean deleteGroup(String groupName, String requestingUser) throws RemoteException;

    public boolean changeGroupOwner(String groupName, String newOwner) throws RemoteException;

    public void sendFile(String sender, String recipient, FileInfo file) throws RemoteException;

    public void registerClient(String username, IChatClient client) throws RemoteException;

    public void unregisterClient(String username) throws RemoteException;

    public boolean deleteUser(String targetUsername, String requestingUsername) throws RemoteException;

    public boolean isAdmin(String username) throws RemoteException;

    public List<String> getPendingRequests(String groupName) throws RemoteException;
}
