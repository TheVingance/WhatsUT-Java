# WhatsUT - Sistema de Chat Distribuído

Este documento explica o funcionamento, arquitetura e funcionalidades do projeto **WhatsUT**, um sistema de mensagens distribuído desenvolvido em Java.

## 📋 Visão Geral do Projeto

O **WhatsUT** é uma aplicação de chat cliente-servidor robusta que permite a troca de mensagens em tempo real entre usuários e grupos. O sistema utiliza **Java RMI (Remote Method Invocation)** para comunicação distribuída, **SQLite** para persistência de dados e **JavaFX** para a interface gráfica do usuário.

## 🛠 Tecnologias Utilizadas

*   **Linguagem**: Java 11
*   **Interface Gráfica**: JavaFX 13
*   **Comunicação Distribuída**: Java RMI
*   **Banco de Dados**: SQLite
*   **Gestão de Dependências**: Maven

## 🚀 Funcionalidades Principais

### 1. Autenticação e Usuários
*   **Registro**: Criação de novas contas com usuário, senha e email. As senhas são armazenadas com hash (segurança).
*   **Login**: Autenticação segura.
*   **Status Online**: Visualização de quais usuários estão online em tempo real (indicador verde/cinza).

### 2. Mensagens Privadas (P2P)
*   **Chat Individual**: Troca de mensagens de texto entre dois usuários.
*   **Histórico**: As mensagens são salvas no banco de dados e recuperadas ao abrir a conversa.
*   **Envio de Arquivos**: Suporte para envio de arquivos anexados nas conversas privadas.

### 3. Grupos de Chat
*   **Criação de Grupos**: Usuários podem criar grupos com nome e descrição.
*   **Sistema de Solicitação**: Para entrar em um grupo, o usuário envia uma solicitação.
*   **Moderação**: O dono do grupo (Owner) pode aprovar ou rejeitar solicitações de entrada.
*   **Gerenciamento**:
    *   O dono pode remover membros.
    *   O dono pode transferir a liderança (propriedade) do grupo.
    *   O dono pode excluir o grupo.
*   **Chat em Grupo**: Mensagens enviadas no grupo são visíveis para todos os membros.

## 🏗 Arquitetura do Sistema

O projeto segue uma arquitetura clássica **Cliente-Servidor** baseada em interfaces RMI:

### Servidor (`server`)
*   Responsável por gerenciar o banco de dados (`DatabaseManager`).
*   Expõe serviços via interface `IChatServer` (registro, login, envio de mensagens, gestão de grupos).
*   Mantém o estado dos clientes conectados.
*   Iniciado via `ServerMain`, que cria o registro RMI na porta `1099`.

### Cliente (`client`)
*   Interface gráfica construída com JavaFX (`ChatUI`).
*   Expõe interface `IChatClient` para receber notificações do servidor (novas mensagens, aprovações de grupo).
*   Conecta-se ao servidor RMI para invocar métodos remotos.

## ⚠️ Observações sobre a Execução e Testes

Durante a análise do código e tentativa de teste, observou-se o seguinte:

1.  **Dependência do Maven**: O projeto usa Maven. Para executar no seu ambiente, é necessário ter o Maven instalado e configurado no PATH (`mvn`).
2.  **Banco de Dados**: O projeto inclui um arquivo `chat.db`. O código verifica e cria tabelas se não existirem, mas foi identificada uma **possível inconsistência**: a tabela `pending_requests` (usada para solicitações de grupos) não está sendo criada automaticamente no método `createTables` do `DatabaseManager.java`. Se o banco for recriado do zero, essa funcionalidade pode falhar até que a tabela seja adicionada manualmente.
3.  **Execução**:
    *   **Servidor**: Deve ser iniciado primeiro (`ServerMain`).
    *   **Cliente**: Pode ter múltiplas instâncias iniciadas (`ChatApp`) para simular diferentes usuários.

## 📝 Como Executar (Requisito: Maven instalado)

1.  **Compilar o Projeto**:
    ```powershell
    mvn clean compile
    ```

2.  **Iniciar o Servidor**:
    ```powershell
    mvn exec:java -Dexec.mainClass="chat.main.ServerMain"
    ```

3.  **Iniciar o Cliente** (em outro terminal):
    ```powershell
    mvn javafx:run
    ```
    *Ou se configurado no plugin exec:*
    ```powershell
    mvn exec:java -Dexec.mainClass="chat.main.ChatApp"
    ```

---
*Este arquivo foi gerado automaticamente após análise do código fonte.*
