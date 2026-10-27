package testes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // Impede erros se a Maria enviar campos extras
public class LogDTO {
    private Long id;
    private String usuario;
    private String timestamp;
    private String data;
    private String acao;

    // Novos campos enviados pela Maria
    private String modelo;
    private Integer ano;
    private Double preco;

    public LogDTO() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }

    @Override
    public String toString() {
        return "LogDTO{" +
                "id=" + id +
                ", usuario='" + usuario + '\'' +
                ", acao='" + acao + '\'' +
                ", modelo='" + modelo + '\'' +
                ", preco=" + preco +
                '}';
    }
}