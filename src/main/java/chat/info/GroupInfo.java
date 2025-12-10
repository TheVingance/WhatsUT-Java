package chat.info;

import java.io.Serializable;
import java.util.*;

public class GroupInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private String owner;
    private List<String> members;
    private List<String> messages;
    private Set<String> pendingRequests; // Solicitações pendentes

    public GroupInfo(String name, String description, String owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.pendingRequests = new HashSet<>();
        this.messages = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    public List<String> getMembers() {
        return members;
    }

    public Set<String> getPendingRequests() {
        return pendingRequests;
    }
    
    public void setOwner(String newOwner) {
        this.owner = newOwner;
    }    

    public void addMessage(String message) {
        messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }

    public boolean removeMember(String username) {
        return members.remove(username);
    }

    public boolean addMember(String username) {
        return members.add(username);
    }

    public boolean isMember(String username) {
        return members.contains(username);
    }

    public void addPendingRequest(String username) {
        pendingRequests.add(username);
    }

    public void removePendingRequest(String username) {
        pendingRequests.remove(username);
    }

    @Override
    public String toString() {
        return "Group{name='" + name + "', owner='" + owner + "', members=" + members.size() + "}";
    }
}
