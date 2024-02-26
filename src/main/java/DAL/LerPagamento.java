package DAL;

import Model.*;
import Utilidades.BaseDados;
import Utilidades.Mensagens;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.*;

import static Utilidades.BaseDados.getConexao;

/**
 * Classe responsável por ler informações sobre pagamentos a partir de uma base de dados.
 */
public class LerPagamento {

    /**
     * Insere as informações de um pagamento na base de dados, incluindo as encomendas associadas.
     *
     * @param pagamento O objeto de pagamento a ser inserido na base de dados.
     * @return true se a inserção for bem-sucedida, false caso contrário.
     * @throws IOException em caso de erro de leitura ou gravação.
     */
    public boolean inserirPagamentoNaBaseDados(Pagamento pagamento) throws IOException {
        Connection conn = null;
        try {
            BaseDados.Ligar();
            conn = getConexao();
            BaseDados.iniciarTransacao(conn);

            // Inserir na tabela Pagamento
            String query = """
                    INSERT INTO Pagamento (referencia, data, id_conta_corrente, id_feira_office)
                        VALUES (?,?,?,?)
                    """;

            try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, pagamento.getReferencia());
                ps.setDate(2, java.sql.Date.valueOf(pagamento.getData()));
                ps.setInt(3, pagamento.getContaCorrente().getId());
                ps.setInt(4, pagamento.getFeiraOffice());

                ps.executeUpdate();

                // Obter o ID gerado na tabela Pagamento
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int idPagamento = generatedKeys.getInt(1);

                    // Inserir na tabela Pagamento_Encomenda
                    String query2 = """
                                INSERT INTO Pagamento_Encomenda (id_encomenda, id_pagamento)
                                VALUES (?, ?)
                            """;

                    try (PreparedStatement ps2 = conn.prepareStatement(query2)) {
                        for (Encomenda encomenda : pagamento.getEncomendas()) {
                            ps2.setInt(1, encomenda.getId());
                            ps2.setInt(2, idPagamento);
                            ps2.executeUpdate();
                        }
                    }


                } else {
                    throw new SQLException("Falha ao obter o ID gerado para Pagamento.");
                }
            }

            BaseDados.commit(conn);
            return true;

        } catch (Exception e) {
            Mensagens.Erro("Erro!", "Erro ao adicionar pagamento à base de dados!");
            assert conn != null;
            BaseDados.rollback(conn);
        } finally {
            BaseDados.Desligar();
        }
        return false;
    }

    /**
     * Lê os dados da empresa Feira & Office na base de dados, incluindo informações bancárias.
     *
     * @return Um objeto FeiraOffice que contem os dados da empresa.
     * @throws SQLException em caso de erro ao acessar a base de dados.
     * @throws IOException  em caso de erro de leitura.
     */
    public FeiraOffice lerDadosDaEmpresa() throws SQLException, IOException {
        FeiraOffice feiraOffice = null;
        try {
            Connection conn = getConexao();

            String query = """
                    SELECT
                        Feira_Office.id AS id,
                    	Feira_Office.nome AS nome,
                    	Feira_Office.morada AS morada,
                    	Feira_Office.localidade AS localidade,
                    	Feira_Office.codigo_postal AS codigo_postal,
                    	Pais.id AS id_pais,
                    	Pais.Nome AS nome_pais,
                    	Pais.ISO AS iso_pais,
                    	Feira_Office.iban AS iban,
                    	Feira_Office.bic AS bic
                                        
                    FROM Feira_Office
                    	INNER JOIN Pais ON Pais.id = Feira_Office.id_pais
                    """;
            try (PreparedStatement statement = conn.prepareStatement(query)) {

                ResultSet resultado = statement.executeQuery();

                if (resultado.next()) {
                    feiraOffice = criarObjetoFeiraOffice(resultado);
                }

            }

        } catch (SQLException e) {
            Mensagens.Erro("Erro!", "Erro ao carregar dados bancários da Feira & Office");
        } finally {
            BaseDados.Desligar();
        }
        return feiraOffice;
    }

    /**
     * Cria e retorna um objeto FeiraOffice com base nos dados fornecidos pelo ResultSet.
     *
     * @param dados O ResultSet que contem os dados da empresa Feira & Office.
     * @return Um objeto FeiraOffice criado com base nos dados fornecidos.
     * @throws SQLException em caso de erro ao acessar os dados do ResultSet.
     */
    private FeiraOffice criarObjetoFeiraOffice(ResultSet dados) throws SQLException {
        Pais pais = new Pais(
                dados.getInt("id_pais"),
                dados.getString("nome_pais"),
                dados.getString("iso_pais")
        );
        return new FeiraOffice(
                dados.getInt("id"),
                dados.getString("nome"),
                dados.getString("morada"),
                dados.getString("localidade"),
                dados.getString("codigo_postal"),
                pais,
                dados.getString("iban"),
                dados.getString("bic")
        );
    }

    /**
     * Verifica se uma referência de pagamento já existe na base de dados.
     *
     * @param pagamento O objeto de pagamento que contem a referência a ser verificada.
     * @return true se a referência existir, false caso contrário.
     * @throws IOException em caso de erro de leitura ou gravação.
     */
    public boolean verificarReferencia(Pagamento pagamento) throws IOException {
        try {
            Connection conn = getConexao();

            String query = """
                    SELECT TOP 1 *
                    FROM Pagamento WHERE referencia = ?
                    """;
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, pagamento.getReferencia());

                // ExecuteQuery para obter resultados de SELECT
                try (ResultSet rs = ps.executeQuery()) {
                    // Se houver resultados, a referência existe
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            Mensagens.Erro("Erro!", "Erro ao verificar referência!");
            System.out.println(e.getMessage());
        } finally {
            BaseDados.Desligar();
        }

        return false;
    }

    /**
     * Obtém a lista de métodos de pagamento a partir da base de dados.
     *
     * @return Uma lista observável de objetos MetodoPagamento.
     * @throws IOException em caso de erro de leitura ou gravação.
     */
    public ObservableList<MetodoPagamento> getMetodos() throws IOException {
        ObservableList<MetodoPagamento> listaDeMetodos = FXCollections.observableArrayList();

        try {
            Connection conn = getConexao();

            String query = "SELECT * FROM Preferencia_Pagamento";

            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                ResultSet resultado = preparedStatement.executeQuery();

                while (resultado.next()) {
                    int id = resultado.getInt("Id");
                    String nome = resultado.getString("Nome");

                    // Mapear valores do banco de dados para a enumeração
                    MetodoPagamento metodoPagamento = mapearMetodoPagamento(id, nome);

                    listaDeMetodos.add(metodoPagamento);
                }
            }

        } catch (SQLException e) {
            Mensagens.Erro("Erro na leitura!", "Erro na leitura da base de dados!");
        } finally {
            BaseDados.Desligar();
        }

        return listaDeMetodos;
    }

    /**
     * Mapeia um identificador e nome de método de pagamento para a enumeração MetodoPagamento.
     *
     * @param id   O identificador do método de pagamento.
     * @param nome O nome do método de pagamento.
     * @return Um objeto MetodoPagamento correspondente ao identificador fornecido.
     * @throws IllegalArgumentException se o identificador do método de pagamento não for reconhecido.
     */
    private MetodoPagamento mapearMetodoPagamento(int id, String nome) {

        return switch (id) {
            case 1 -> MetodoPagamento.DebitoDireto;
            case 2 -> MetodoPagamento.Transferencia;

            default -> throw new IllegalArgumentException("Método de pagamento não reconhecido: " + id);
        };
    }

}
