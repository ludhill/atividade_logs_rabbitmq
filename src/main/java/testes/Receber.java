package testes;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class Receber {

    private static final String IP_PROFESSOR = "10.209.1.53";

    private static final String NOME_DA_FILA = "dupla_maria_lucas";

    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(IP_PROFESSOR);
            factory.setPort(5672);
            factory.setUsername("admin");
            factory.setPassword("admin");

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // Declara a fila no RabbitMQ para garantir que ela existe
            channel.queueDeclare(NOME_DA_FILA, true, false, false, null);

            System.out.println(">>> [CONECTADO] Aguardando mensagens na fila '" + NOME_DA_FILA + "'...");

            // Callback ativado assim que uma mensagem chega
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("\n------------------------------------------------");
                System.out.println(">>> MENSAGEM RECEBIDA DA COLEGA:");
                System.out.println(">>> " + message);
                System.out.println("------------------------------------------------\n");
            };

            channel.basicConsume(NOME_DA_FILA, true, deliverCallback, consumerTag -> {});

        } catch (Exception e) {
            System.err.println(">>> [ERRO] Não foi possível conectar ao servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}