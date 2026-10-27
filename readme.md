# Módulo Consumidor de Logs (`ReceberLog`)

Este módulo implementa a recepção assíncrona de logs vindos de um servidor RabbitMQ, realizando o tratamento de inconsistências de schema JSON e garantindo **dupla persistência** (arquivo local `.json` e banco de dados MySQL).

---

## 🏗️ Estrutura do Projeto

* **Queue RabbitMQ:** `dupla_maria_lucas` (Host: `10.209.1.XX:5672`) // substitua pelo host #de ping no ip
* **Banco de Dados:** MySQL `db_logs_auditoria` (Tabela: `tb_log_operacoes`)
* **Arquivo Local:** `logs_operacoes.json`

---

## 📄 Classes e Métodos

### 1. `LogDTO.java`

Classe de transferência de dados (DTO) responsável por mapear o payload JSON recebido da fila para objetos Java.

* **Anotação `@JsonIgnoreProperties(ignoreUnknown = true)**`: Evita erros de conversão (`UnrecognizedPropertyException`) caso o produtor envie campos não mapeados no DTO.
* **Atributos principais**: `id`, `usuario`, `timestamp`, `data`, `acao`, `modelo`, `ano`, `preco`.
* **Getters e Setters**: Permitem a manipulação e leitura segura das propriedades desserializadas pelo `ObjectMapper` do Jackson.

---

### 2. `ReceberLog.java`

Classe executável (`main`) responsável pela conexão com o broker, escuta de mensagens e gerenciamento da persistência.

#### Métodos:

* **`main(String[] args)`**
* Configura as credenciais e a conexão com o servidor RabbitMQ via `ConnectionFactory`.
* Declara e passa a escutar a fila `dupla_maria_lucas`.
* Utiliza `DeliverCallback` para receber a mensagem em `UTF-8`.
* Executa a estratégia **Resilient First**: salva o payload bruto em arquivo antes de tentar converter para DTO e gravar no banco.


* **`salvarEmArquivo(String conteudo)`**
* Salva o JSON exatamente como foi recebido em um arquivo local (`logs_operacoes.json`).
* Garante a recuperação de dados brutos (*disaster recovery*) caso ocorra falha no banco de dados ou no parse do JSON.


* **`salvarNoMySQL(String json, LogDTO dto)`**
* Insere os dados mapeados na tabela `tb_log_operacoes` do MySQL via `PreparedStatement`.
* **Tratamento de Contingência (Prevenção de `NULL`)**:
* Se `timestamp` ou `data` não forem enviados no JSON, gera automaticamente o timestamp atual (`LocalDateTime.now()`).
* Se `usuario` ou `acao` não forem enviados, constrói uma identificação padrão com base nos dados presentes (ex: modelo do veículo).





---

## 🛠️ Como Executar

1. Certifique-se de que o MySQL está rodando e a tabela `tb_log_operacoes` foi criada.
2. Execute a classe `ReceberLog.java`.
3. O console exibirá: `>>> [MÓDULO DE LOGS ATIVO] Escutando a fila 'dupla_maria_lucas'...`.
4. As mensagens recebidas serão gravadas simultaneamente no disco e no MySQL.