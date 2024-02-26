package Utilidades;

import java.io.IOException;
import java.sql.*;

/**
 * Métodos de conexão com a base de Dados SQL
 */
public class BaseDados {
    // credenciais de acesso ao SQL SERVER
    public static final String url = "";
    public static final String username = "";
    public static final String password = "";

    private static Connection connection; // a ligação ao SQL


    /**
     * Obtém uma conexão com o banco de dados.
     *
     * @return A conexão com o banco de dados.
     */
    public static Connection getConexao() {
        Connection conexao = null;
        try {
            conexao = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace(); // Tratamento de exceção adequado ao seu código
        }

        return conexao;
    }

    /**
     * Inicia uma transação na conexão.
     *
     * @param conexao A conexão com o banco de dados.
     */
    public static void iniciarTransacao(Connection conexao) throws IOException {
        try {
            conexao.setAutoCommit(false);
        } catch (SQLException e) {
            Mensagens.Erro("Erro!","Erro ao iniciar transação!");
        }
    }

    /**
     * Realiza o commit da transação na conexão.
     *
     * @param conexao A conexão com o banco de dados.
     */
    public static void commit(Connection conexao) throws IOException, SQLException {
        try {
            conexao.commit();
        } catch (SQLException e) {
            Mensagens.Erro("Erro!", "Erro ao realizar commit!");
            System.out.println(e.getMessage());
            conexao.rollback(); // Em caso de falha, realiza rollback
        } finally {
            try {
                conexao.setAutoCommit(true);
            } catch (SQLException e) {
                Mensagens.Erro("Erro!", "Erro ao restaurar o estado AutoCommit!");
                System.out.println(e.getMessage());
                // Considere logar a exceção ou tratar de forma mais apropriada
            }
        }
    }

    /**
     * Realiza o rollback da transação na conexão.
     *
     * @param conexao A conexão com o banco de dados.
     */
    public static void rollback(Connection conexao) throws IOException {
        try {
            if (!conexao.getAutoCommit()) {
                conexao.rollback();
            }
        } catch (SQLException e) {
            Mensagens.Erro("Erro!", "Erro ao realizar rollback!" + e.getMessage());
        }
    }


    /**
     * Estabelece uma ligação com o SQL Server.
     *
     */

    public static void Ligar() {
        try {
            DriverManager.getConnection(url, username, password);
            //return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Termina a ligação com o SQL Server.
     *
     */
    public static void Desligar() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Define a conexão a ser usada por esta classe para interagir com a base de dados.
     *
     * @param connection A conexão a ser definida.
     */
    public static void setConnection(Connection connection) {
        BaseDados.connection = connection;
    }


    /**
     * Executa uma consulta SQL que não retorna um conjunto de resultados (por exemplo, INSERT, UPDATE, DELETE).
     *
     * @param query A consulta SQL a ser executada.
     * @return true se a consulta for bem-sucedida, false caso contrário.
     */
    public static boolean Executar(String query, Connection connection) {
        try {
            //se já foi invocado o ligar e a ligação está valida então envia o comando da query
            if (connection != null && !connection.isClosed()) {
                Statement script = connection.createStatement();
                script.execute(query); //executa o script e aguarda true ou false (boolean)
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}

