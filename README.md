# WhatsUT - Sistema de Chat Distribuído

## 📖 Sobre o Projeto
**WhatsUT** é uma aplicação de mensagens instantâneas desenvolvida em Java, utilizando **Java RMI (Remote Method Invocation)** para comunicação distribuída. O sistema permite a troca de mensagens em tempo real, criação de grupos e transferência de arquivos, com uma interface gráfica moderna construída em **JavaFX**.

### 🚀 Funcionalidades Principais
*   **Autenticação**: Login e Registro de usuários seguros.
*   **Chat Privado**: Troca de mensagens diretas entre usuários.
*   **Chat em Grupo**: Criação, gerenciamento e interação em grupos.
*   **Transferência de Arquivos**: Envio de arquivos entre usuários.
*   **Persistência**: Dados armazenados em banco de dados **SQLite**.
*   **Administração**:
    *   Usuário `admin` pode excluir contas de usuários.
    *   Usuário `admin` e donos de grupos podem excluir grupos.

---

## 🛠️ Tecnologias Utilizadas
*   **Linguagem**: Java 21
*   **Interface Gráfica**: JavaFX
*   **Comunicação**: Java RMI
*   **Banco de Dados**: SQLite
*   **Gerenciamento de Dependências**: Maven

---

## ⚙️ Pré-requisitos
Para executar este projeto, você precisará ter instalado:
*   [Java JDK 21](https://www.oracle.com/java/technologies/downloads/#java21) ou superior.
*   [Apache Maven](https://maven.apache.org/download.cgi).
*   [Git](https://git-scm.com/downloads) (opcional, para clonar o repositório).

---

## 🏃 Como Executar

O sistema é composto por duas partes: **Servidor** e **Cliente**. Eles devem ser executados em terminais separados.

### 1. Clonar o Repositório
```bash
git clone https://github.com/TheVingance/WhatsUT-Java.git
cd WhatsUT-Java/whatsut
```

### 2. Executar o Servidor
O servidor deve ser iniciado primeiro. Ele gerencia as conexões e o banco de dados.

Abra um terminal na pasta `whatsut` e execute:
```bash
mvn exec:java
```
*Aguarde até ver a mensagem de que o servidor está rodando.*

### 3. Executar o Cliente
Com o servidor rodando, abra um **novo terminal** (na mesma pasta `whatsut`) e inicie o cliente:
```bash
mvn javafx:run
```
*A janela de login do WhatsUT deve abrir.*

> **Nota:** Você pode abrir múltiplos terminais e executar `mvn javafx:run` várias vezes para simular múltiplos usuários conversando.

---

## 🔐 Credenciais de Administrador
O sistema cria automaticamente um usuário administrador na primeira execução.

*   **Usuário**: `admin`
*   **Senha**: `admin`

Use essas credenciais para testar as funcionalidades administrativas (excluir usuários/grupos).

---

## 📂 Estrutura do Projeto
*   `src/main/java/chat/`: Código fonte principal.
    *   `chat/server/`: Implementação do Servidor RMI.
    *   `chat/client/`: Implementação do Cliente e Interface Gráfica.
    *   `chat/database/`: Gerenciamento do SQLite.
*   `target/`: Arquivos compilados (gerados pelo Maven).
*   `chat.db`: Arquivo do banco de dados (gerado automaticamente na raiz na primeira execução).
