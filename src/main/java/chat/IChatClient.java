package chat;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IChatClient extends Remote {
    public String getUsername() throws RemoteException;

    public List<String> getUnreadMessages(String sender) throws RemoteException;

    public void clearUnreadMessages(String sender) throws RemoteException;

    public void receiveMessage(String sender, String message) throws RemoteException;

    public boolean login(String password) throws RemoteException;

    public void logout() throws RemoteException;

    public void sendMessage(String recipient, String message) throws RemoteException;

    public java.util.List<String> getUserList() throws RemoteException;

    void notifyNewMessage(String sender) throws RemoteException;

    public void notifyGroupJoinApproval(String groupName, boolean approved) throws RemoteException;

    public void notifyGroupRemoval(String groupName) throws RemoteException;

    public void notifyNewGroupOwner(String groupName) throws RemoteException;

    public boolean deleteUser(String password) throws RemoteException;
}
