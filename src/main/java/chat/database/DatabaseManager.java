package chat.database;

import chat.info.*;
import chat.utils.*;

import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:chat.db"; // Nome do banco SQLite

    public DatabaseManager() {
        createTables(); // Cria as tabelas ao iniciar o banco
        updateSchema(); // Atualiza o esquema se necessário
        createDefaultAdmin(); // Garante que o admin existe
    }

    private void updateSchema() {
        String sql = "ALTER TABLE users ADD COLUMN is_admin BOOLEAN DEFAULT 0";
        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Coluna 'is_admin' adicionada com sucesso.");
        } catch (SQLException e) {
            // Se a coluna já existir, vai dar erro, então apenas ignoramos
            if (!e.getMessage().contains("duplicate column name")) {
                // System.out.println("Nota: Coluna 'is_admin' já deve existir ou erro: " +
                // e.getMessage());
            }
        }
    }

    private void createDefaultAdmin() {
        if (!userExists("admin")) {
            registerUser("admin", "admin", "admin@whatsut.com");
            System.out.println("⚠️ Admin user created: admin / admin");
        }
        // Sempre garante que o admin tenha permissão, mesmo se já existir
        makeAdmin("admin");
    }

    private boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    private void makeAdmin(String username) {
        String sql = "UPDATE users SET is_admin = 1 WHERE username = ?";
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isAdmin(String username) {
        String sql = "SELECT is_admin FROM users WHERE username = ?";
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_admin");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Conectar ao banco de dados SQLite
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Criar tabelas no banco
    private void createTables() {
        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {

            // Criar tabela de usuários
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                    + "username TEXT PRIMARY KEY, "
                    + "password TEXT NOT NULL, "
                    + "email TEXT NOT NULL, "
                    + "is_admin BOOLEAN DEFAULT 0);"; // Adicionado coluna is_admin

            // Criar tabela de mensagens
            String createMessagesTable = "CREATE TABLE IF NOT EXISTS messages ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "sender TEXT NOT NULL, "
                    + "recipient TEXT NOT NULL, "
                    + "message TEXT, "
                    + "timestamp INTEGER NOT NULL, "
                    + "file_name TEXT, "
                    + "file_data BLOB, "
                    + "FOREIGN KEY (sender) REFERENCES users(username), "
                    + "FOREIGN KEY (recipient) REFERENCES users(username));";

            // Criar tabela de grupos
            String createGroupsTable = "CREATE TABLE IF NOT EXISTS groups ("
                    + "name TEXT PRIMARY KEY, "
                    + "description TEXT, "
                    + "owner TEXT NOT NULL, "
                    + "FOREIGN KEY (owner) REFERENCES users(username));";

            // Criar tabela de membros de grupos
            String createGroupMembersTable = "CREATE TABLE IF NOT EXISTS group_members ("
                    + "group_name TEXT NOT NULL, "
                    + "username TEXT NOT NULL, "
                    + "FOREIGN KEY (group_name) REFERENCES groups(name), "
                    + "FOREIGN KEY (username) REFERENCES users(username), "
                    + "PRIMARY KEY (group_name, username));";

            stmt.execute(createUsersTable);
            stmt.execute(createMessagesTable);
            stmt.execute(createGroupsTable);
            stmt.execute(createGroupMembersTable);

            System.out.println("✅ Tabelas do banco de dados criadas com sucesso!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean registerUser(String username, String password, String email) {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, HashUtil.generateHash(password));
            pstmt.setString(3, email);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Erro ao registrar usuário: " + e.getMessage());
            return false;
        }
    }

    public boolean login(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                return storedHash.equals(HashUtil.generateHash(password));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao fazer login: " + e.getMessage());
        }
        return false;
    }

    public List<String> listUsers() {
        List<String> users = new ArrayList<>();
        String sql = "SELECT username FROM users";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(rs.getString("username"));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar usuários: " + e.getMessage());
        }
        return users;
    }

    public void sendMessage(String sender, String recipient, String message, long timestamp) throws RemoteException {
        MessageInfo msg = new MessageInfo(sender, recipient, message, timestamp);
        storeMessage(msg);
    }

    public void sendFile(String sender, String recipient, FileInfo file, long timestamp) throws RemoteException {
        MessageInfo msg = new MessageInfo(sender, recipient, file, timestamp);
        storeMessage(msg);
    }

    public void storeMessage(MessageInfo msg) {
        String sql = "INSERT INTO messages (sender, recipient, message, timestamp, file_name, file_data) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, msg.getSender());
            pstmt.setString(2, msg.getRecipient());
            pstmt.setString(3, msg.getMessage());
            pstmt.setLong(4, msg.getTimestamp());

            if (msg.isFile() && msg.getFile() != null) {
                pstmt.setString(5, msg.getFile().getFileName());
                pstmt.setBytes(6, msg.getFile().getFileData());
            } else {
                pstmt.setNull(5, Types.VARCHAR); // Se não for um arquivo, define o file_name como null
                pstmt.setNull(6, Types.BLOB); // Se não for um arquivo, define file_data como null
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao armazenar mensagem: " + e.getMessage());
        }
    }

    public List<MessageInfo> getMessageHistory(String user1, String user2) throws RemoteException {
        String sql = "SELECT sender, recipient, message, timestamp, file_name, file_data "
                + "FROM messages WHERE (sender = ? AND recipient = ?) OR (sender = ? AND recipient = ?) "
                + "ORDER BY timestamp ASC";

        List<MessageInfo> messages = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String sender = rs.getString("sender");
                String recipient = rs.getString("recipient");
                String message = rs.getString("message");
                long timestamp = rs.getLong("timestamp");
                String fileName = rs.getString("file_name");
                byte[] fileData = rs.getBytes("file_data");

                if (fileName != null) {
                    FileInfo fileInfo = new FileInfo(fileName, fileData);
                    messages.add(new MessageInfo(sender, recipient, fileInfo, timestamp));
                } else {
                    messages.add(new MessageInfo(sender, recipient, message, timestamp));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar histórico de mensagens: " + e.getMessage());
        }

        return messages;
    }

    public FileInfo receiveFile(String sender, String recipient, String fileName) throws RemoteException {
        String sql = "SELECT file_name, file_data FROM messages WHERE sender = ? AND recipient = ? AND file_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sender);
            pstmt.setString(2, recipient);
            pstmt.setString(3, fileName);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                byte[] fileData = rs.getBytes("file_data");
                return new FileInfo(fileName, fileData);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao receber arquivo: " + e.getMessage());
        }
        return null; // Retorna null se o arquivo não for encontrado
    }

    public boolean createGroup(String groupName, String description, String owner) {
        String sql = "INSERT INTO groups (name, description, owner) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupName);
            pstmt.setString(2, description);
            pstmt.setString(3, owner);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                addGroupMember(groupName, owner); // O dono do grupo entra automaticamente
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao criar grupo: " + e.getMessage());
        }
        return false;
    }

    public boolean addGroupMember(String groupName, String username) {
        String sql = "INSERT INTO group_members (group_name, username) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupName);
            pstmt.setString(2, username);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao adicionar membro ao grupo: " + e.getMessage());
        }
        return false;
    }

    public boolean removeGroupMember(String groupName, String username) {
        String sql = "DELETE FROM group_members WHERE group_name = ? AND username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupName);
            pstmt.setString(2, username);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao remover membro do grupo: " + e.getMessage());
        }
        return false;
    }

    public void sendGroupMessage(String groupName, String sender, String message) {
        String sql = "INSERT INTO group_messages (group_name, sender, message, timestamp) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, groupName);
            pstmt.setString(2, sender);
            pstmt.setString(3, message);
            pstmt.setLong(4, System.currentTimeMillis());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao enviar mensagem no grupo: " + e.getMessage());
        }
    }

    public List<String> getGroupMessages(String groupName) {
        String sql = "SELECT sender, message, timestamp FROM group_messages WHERE group_name = ? ORDER BY timestamp ASC";
        List<String> messages = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String sender = rs.getString("sender");
                String message = rs.getString("message");
                long timestamp = rs.getLong("timestamp"); // O timestamp no banco é do tipo long
                String horaFormatada = sdf.format(new Date(timestamp));

                messages.add(sender + ": " + message + " [" + horaFormatada + "]");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar mensagens do grupo: " + e.getMessage());
        }

        return messages;
    }

    public boolean deleteGroup(String groupName) {
        String deleteMessages = "DELETE FROM group_messages WHERE group_name = ?";
        String deleteMembers = "DELETE FROM group_members WHERE group_name = ?";
        String deleteGroup = "DELETE FROM groups WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt1 = conn.prepareStatement(deleteMessages);
                PreparedStatement pstmt2 = conn.prepareStatement(deleteMembers);
                PreparedStatement pstmt3 = conn.prepareStatement(deleteGroup)) {

            pstmt1.setString(1, groupName);
            pstmt1.executeUpdate();

            pstmt2.setString(1, groupName);
            pstmt2.executeUpdate();

            pstmt3.setString(1, groupName);
            return pstmt3.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erro ao excluir grupo: " + e.getMessage());
        }
        return false;
    }

    public GroupInfo getGroupInfo(String groupName) {
        String sql = "SELECT * FROM groups WHERE name = ?";
        GroupInfo group = null;

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String description = rs.getString("description");
                String owner = rs.getString("owner");

                group = new GroupInfo(groupName, description, owner);

                // Buscar membros do grupo
                String memberSql = "SELECT username FROM group_members WHERE group_name = ?";
                try (PreparedStatement memberStmt = conn.prepareStatement(memberSql)) {
                    memberStmt.setString(1, groupName);
                    ResultSet memberRs = memberStmt.executeQuery();
                    while (memberRs.next()) {
                        group.addMember(memberRs.getString("username"));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar informações do grupo: " + e.getMessage());
        }

        return group;
    }

    public boolean changeGroupOwner(String groupName, String newOwner) {
        String sql = "UPDATE groups SET owner = ? WHERE name = ? AND EXISTS (SELECT 1 FROM group_members WHERE group_name = ? AND username = ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newOwner);
            pstmt.setString(2, groupName);
            pstmt.setString(3, groupName);
            pstmt.setString(4, newOwner);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao alterar dono do grupo: " + e.getMessage());
        }
        return false;
    }

    public List<String> listGroups() {
        String sql = "SELECT name FROM groups";
        List<String> groupList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                groupList.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar grupos: " + e.getMessage());
        }

        return groupList;
    }

    public boolean requestJoinGroup(String groupName, String username) {
        String sql = "INSERT OR IGNORE INTO pending_requests (group_name, username) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupName);
            stmt.setString(2, username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean approveJoinRequest(String groupName, String owner, String username, boolean approve) {
        if (approve) {
            String insertSql = "INSERT INTO group_members (group_name, username) VALUES (?, ?)";
            String deleteSql = "DELETE FROM pending_requests WHERE group_name = ? AND username = ?";

            try (Connection conn = DriverManager.getConnection(DB_URL);
                    PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                    PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

                conn.setAutoCommit(false);

                // Adicionar usuário ao grupo
                insertStmt.setString(1, groupName);
                insertStmt.setString(2, username);
                insertStmt.executeUpdate();

                // Remover solicitação pendente
                deleteStmt.setString(1, groupName);
                deleteStmt.setString(2, username);
                deleteStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            // Apenas remover solicitação se for rejeitada
            String deleteSql = "DELETE FROM pending_requests WHERE group_name = ? AND username = ?";

            try (Connection conn = DriverManager.getConnection(DB_URL);
                    PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setString(1, groupName);
                stmt.setString(2, username);
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public List<String> getPendingRequests(String groupName) {
        List<String> pendingUsers = new ArrayList<>();
        String sql = "SELECT username FROM pending_requests WHERE group_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                pendingUsers.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pendingUsers;
    }

    public boolean deleteUser(String username) {
        String deleteMessages = "DELETE FROM messages WHERE sender = ? OR recipient = ?";
        String deleteGroupMessages = "DELETE FROM group_messages WHERE sender = ?";
        String deleteGroupMembers = "DELETE FROM group_members WHERE username = ?";
        String deleteGroups = "DELETE FROM groups WHERE owner = ?"; // Isso pode ser perigoso se não tratar os grupos
                                                                    // órfãos ou deletar tudo
        // Na verdade, se deletar o grupo, precisa deletar os membros e mensagens
        // daquele grupo primeiro?
        // SQLite com ON DELETE CASCADE seria melhor, mas vamos fazer manual por
        // segurança/simplicidade do código existente.

        // Estratégia:
        // 1. Deletar mensagens privadas (enviadas ou recebidas).
        // 2. Deletar mensagens de grupo enviadas pelo usuário.
        // 3. Remover usuário de grupos (group_members).
        // 4. Lidar com grupos onde ele é dono:
        // Opção A: Deletar o grupo inteiro (mais simples).
        // Opção B: Passar posse (muito complexo para agora).
        // Vamos na Opção A -> Para cada grupo que ele é dono, chamar
        // deleteGroup(groupName).

        String selectOwnedGroups = "SELECT name FROM groups WHERE owner = ?";
        String deleteUser = "DELETE FROM users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            // 1. Deletar mensagens privadas
            try (PreparedStatement pstmt = conn.prepareStatement(deleteMessages)) {
                pstmt.setString(1, username);
                pstmt.setString(2, username);
                pstmt.executeUpdate();
            }

            // 2. Deletar mensagens de grupo
            try (PreparedStatement pstmt = conn.prepareStatement(deleteGroupMessages)) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }

            // 3. Remover de grupos
            try (PreparedStatement pstmt = conn.prepareStatement(deleteGroupMembers)) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }

            // 4. Deletar grupos que ele é dono
            try (PreparedStatement pstmt = conn.prepareStatement(selectOwnedGroups)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    deleteGroup(rs.getString("name")); // Reutiliza o método existente que limpa tudo do grupo
                }
            }

            // 5. Deletar o usuário
            try (PreparedStatement pstmt = conn.prepareStatement(deleteUser)) {
                pstmt.setString(1, username);
                return pstmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao deletar usuário: " + e.getMessage());
        }
        return false;
    }

}
