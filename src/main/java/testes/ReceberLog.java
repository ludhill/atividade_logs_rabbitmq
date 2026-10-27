package testes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;


import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReceberLog {

    private static final String IP_PROFESSOR = "10.209.1.53";
    private static final String FILA_LOGS = "dupla_maria_lucas";
    private static final String ARQUIVO_LOG = "logs_operacoes.json";

    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/db_logs_auditoria?useSSL=false&serverTimezone=UTC";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASS = "";

    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(IP_PROFESSOR);
            factory.setPort(5672);
            factory.setUsername("admin");
            factory.setPassword("admin");

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // Declara a fila de logs
            channel.queueDeclare(FILA_LOGS, true, false, false, null);
            System.out.println(">>> [MÓDULO DE LOGS ATIVO] Escutando a fila '" + FILA_LOGS + "'...");

            // Instancia o leitor de JSON do Jackson
            ObjectMapper mapper = new ObjectMapper();

            // Processador da mensagem recebida
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String jsonRecebido = new String(delivery.getBody(), StandardCharsets.UTF_8);

                System.out.println(">>> [MENSAGEM RECEBIDA]: " + jsonRecebido);

                // 1. Redundância 1: Salva no arquivo local (.json)
                salvarEmArquivo(jsonRecebido);

                // Parse do DTO para extrair os campos
                LogDTO dto = null;
                try {
                    dto = mapper.readValue(jsonRecebido, LogDTO.class);
                } catch (Exception e) {
                    System.err.println(">>> Falha na conversão para DTO (o payload bruto será salvo mesmo assim): " + e.getMessage());
                }

                // 2. Redundância 2: Salva na tabela do MySQL
                salvarNoMySQL(jsonRecebido, dto);
            };

            channel.basicConsume(FILA_LOGS, true, deliverCallback, consumerTag -> {});

        } catch (Exception e) {
            System.err.println(">>> Erro no Módulo de Logs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método responsável por persistir o log em disco
    private static void salvarEmArquivo(String conteudo) {
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String registro = "[" + dataHora + "] LOG: " + conteudo;

        try (FileWriter fw = new FileWriter(ARQUIVO_LOG, true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println(registro);
            System.out.println(">>> [LOG SALVO COM SUCESSO] Registrado em '" + ARQUIVO_LOG + "'");

        } catch (Exception e) {
            System.err.println(">>> Erro ao escrever no arquivo de log: " + e.getMessage());
        }
    }

    private static void salvarNoMySQL(String json, LogDTO dto) {
        String sql = "INSERT INTO tb_log_operacoes (origem_id, usuario, timestamp_operacao, data_operacao, acao, payload_json) VALUES (?, ?, ?, ?, ?, ?)";

        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            // ID de origem (ex: ID 16 do carro)
            if (dto != null && dto.getId() != null) {
                stmt.setLong(1, dto.getId());
            } else {
                stmt.setNull(1, java.sql.Types.BIGINT);
            }

            // Usuário
            String usuario = (dto != null && dto.getUsuario() != null) ? dto.getUsuario() : "Maria (Sistema Carros)";
            stmt.setString(2, usuario);

            // Timestamp e Data
            stmt.setString(3, dto != null ? dto.getTimestamp() : null);
            stmt.setString(4, dto != null ? dto.getData() : null);

            // Ação: Se não vier o campo acao explícito, descreve com base no modelo/carro
            String acao;
            if (dto != null && dto.getAcao() != null) {
                acao = dto.getAcao();
            } else if (dto != null && dto.getModelo() != null) {
                acao = "LOG DE CARRO: " + dto.getModelo() + " (Ano " + dto.getAno() + ")";
            } else {
                acao = "OPERACAO DESCONHECIDA";
            }
            stmt.setString(5, acao);

            // Payload JSON completo
            stmt.setString(6, json);

            stmt.executeUpdate();
            System.out.println(">>> [REDUNDÂNCIA MYSQL] Log inserido no banco com sucesso!");

        } catch (Exception e) {
            System.err.println(">>> [ERRO MYSQL] Falha ao salvar no banco de dados: " + e.getMessage());
        }
    }
}