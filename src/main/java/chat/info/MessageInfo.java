package chat.info;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String sender;
    private String recipient;
    private String message;
    private long timestamp;
    private FileInfo file;  // Pode ser um arquivo ou null
    private boolean isFile; // Indica se é uma mensagem de arquivo ou texto

    public MessageInfo(String sender, String recipient, String message, long timestamp) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
        this.timestamp = timestamp; // Armazena a hora da mensagem
        this.isFile = false;
    }

    public MessageInfo(String sender, String recipient, FileInfo file, long timestamp) {
        this.sender = sender;
        this.recipient = recipient;
        this.timestamp = timestamp; // Armazena a hora da mensagem
        this.isFile = true;
        this.file = file;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public FileInfo getFile() {
        return file;
    }

    public boolean isFile() {
        return isFile;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String horaFormatada = sdf.format(new Date(timestamp));
        if (isFile) {
            return sender + " enviou um arquivo: " + file.getFileName() + " [" + horaFormatada + "]";
        } else {
            return sender + ": " + message + " [" + horaFormatada + "]";
        }
    }
}

