package testes;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;

public class Enviar {

    private static final String IP_PROFESSOR = "10.209.1.87";

    public static void main(String[] args) {

        enviarMensagem("Ldenner_,12345", "fila_de_maria");
    }

    public static void enviarMensagem(String message, String queue) {
        ConnectionFactory factory = new ConnectionFactory();

        try {
            factory.setHost(IP_PROFESSOR);
            factory.setPort(5672);
            factory.setUsername("admin");
            factory.setPassword("admin");

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.queueDeclare(queue, true, false, false, null);
                channel.basicPublish("", queue, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(">>> [SUCESSO] Mensagem enviada para " + IP_PROFESSOR + " na fila '" + queue + "': " + message);
            }
        } catch (Exception e) {
            System.err.println(">>> [ERRO] Não foi possível conectar ao RabbitMQ no IP " + IP_PROFESSOR);
            e.printStackTrace();
        }
    }
}