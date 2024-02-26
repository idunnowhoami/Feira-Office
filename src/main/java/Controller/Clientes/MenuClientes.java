package Controller.Clientes;

import DAL.LerClientes;
import Model.API.Cliente;
import Utilidades.API;
import Utilidades.Mensagens;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

import static Utilidades.API.deleteCliente;

public class MenuClientes {
    LerClientes lerClientes = new LerClientes();

    @FXML
    private Button btnAprovar;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnRecusar;

    @FXML
    private TableView<Cliente> tableViewClientes;
    @FXML
    private AnchorPane anchorPaneEncomendasWeb;

    ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    /**
     * Inicializa a classe, ao carregar a tabela de clientes.
     *
     * @throws IOException Se ocorrer um erro durante a leitura dos clientes.
     */
    public void initialize() throws IOException {
        tabelaClientes();
    }
    /**
     * Preenche a TableView com os dados dos clientes.
     *
     * @throws IOException Se ocorrer um erro durante a leitura dos clientes.
     */
    public void tabelaClientes() throws IOException {

        clientes.addAll(lerClientes.lerClientesDaApi());

        if (!clientes.isEmpty()) {
            if (tableViewClientes.getItems().isEmpty()) {
                TableColumn<Cliente, String> colunaId = new TableColumn<>("ID");
                TableColumn<Cliente, String> nomeColuna = new TableColumn<>("Nome");
                TableColumn<Cliente, String> emailColuna = new TableColumn<>("E-mail");
                TableColumn<Cliente, String> colunaMorada1 = new TableColumn<>("Morada 1");
                TableColumn<Cliente, String> colunaMorada2 = new TableColumn<>("Morada 2");
                TableColumn<Cliente, String> colunaCodPostal = new TableColumn<>("Código postal");
                TableColumn<Cliente, String> colunaCidade = new TableColumn<>("Cidade");
                TableColumn<Cliente, String> colunaPais = new TableColumn<>("País");
                TableColumn<Cliente, String> colunaNif = new TableColumn<>("NIF");
                TableColumn<Cliente, String> colunaEstado = new TableColumn<>("Estado");

                // Associe as propriedades dos objetos Cliente às colunas
                colunaId.setCellValueFactory(new PropertyValueFactory<>("Id"));
                nomeColuna.setCellValueFactory(new PropertyValueFactory<>("Name"));
                emailColuna.setCellValueFactory(new PropertyValueFactory<>("Email"));
                colunaMorada1.setCellValueFactory(new PropertyValueFactory<>("Address1"));
                colunaMorada2.setCellValueFactory(new PropertyValueFactory<>("Address2"));
                colunaCodPostal.setCellValueFactory(new PropertyValueFactory<>("PostalCode"));
                colunaCidade.setCellValueFactory(new PropertyValueFactory<>("City"));
                colunaPais.setCellValueFactory(new PropertyValueFactory<>("Country"));
                colunaNif.setCellValueFactory(new PropertyValueFactory<>("TaxIdentificationNumber"));
                colunaEstado.setCellValueFactory(new PropertyValueFactory<>("Active"));

                // Adicione as colunas à TableView
                tableViewClientes.getColumns().addAll(colunaId, nomeColuna, emailColuna, colunaMorada1, colunaMorada2, colunaCodPostal,
                        colunaCidade, colunaPais, colunaNif, colunaEstado);

                // Adicione os dados à TableView
                tableViewClientes.setItems(clientes);
            } else {
                Mensagens.Erro("Erro!", "Erro ao ler tabela ou tabela vazia!");
            }

        }

    }
    /**
     * Manipula o evento de clique no botão "Aprovar" na interface gráfica.
     * Aprova um cliente selecionado, ao alterar o estado para ativo na API.
     *
     * @param event O evento de clique do botão.
     * @throws IOException Se ocorrer um erro ao aprovar o cliente.
     */
    @FXML
    void clickAprovar(ActionEvent event) throws IOException {
        Cliente clienteSelecionado = tableViewClientes.getSelectionModel().getSelectedItem();

        if (clienteSelecionado != null) {

            String idCliente = clienteSelecionado.getId();

            try {

                String data = "{ \"Active\": true }";
                API.updateCliente(idCliente, data);
                Mensagens.Informacao("Cliente Aprovado!", "Cliente Aprovado com sucesso");


            } catch (IOException e) {
                Mensagens.Erro("Erro!", "Erro ao aprovar cliente!");
            }
        }
    }
    /**
     * Manipula o evento de clique no botão "Encomendas Web" na interface gráfica.
     * Carrega a interface de menu de encomendas web quando o botão é clicado.
     *
     * @param event O evento de clique do botão.
     */
    @FXML
    void clickEncomendasWeb(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lp3/Views/Clientes/menuEncomendasWeb.fxml"));
            AnchorPane root = loader.load();

            anchorPaneEncomendasWeb.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Manipula o evento de clique no botão "Eliminar" na interface gráfica.
     * Elimina o cliente selecionado, removendo-o tanto da API quanto da tabela de clientes.
     *
     * @param event O evento de clique do botão.
     * @throws IOException Se ocorrer um erro ao eliminar o cliente.
     */
    @FXML
    void clickEliminar(ActionEvent event) throws IOException {
        Cliente clienteSelecionado = tableViewClientes.getSelectionModel().getSelectedItem();

        if (clienteSelecionado != null) {

            String idCliente = clienteSelecionado.getId();

            try {
                deleteCliente(idCliente);
                tableViewClientes.getItems().remove(clienteSelecionado);
                Mensagens.Informacao("Cliente Eliminado!", "Cliente Eliminado com sucesso");


            } catch (IOException e) {
                Mensagens.Erro("Erro!", "Erro ao Eliminar cliente!");
            }
        }

    }


    /**
     * Manipula o evento de clique no botão "Recusar" na interface gráfica.
     * Atualiza o status do cliente selecionado para inativo, indica sua recusa.
     *
     * @param event O evento de clique do botão.
     * @throws IOException Se ocorrer um erro ao recusar o cliente.
     */
    @FXML
    void clickRecusar(ActionEvent event) throws IOException {

        Cliente clienteSelecionado = tableViewClientes.getSelectionModel().getSelectedItem();

        if (clienteSelecionado != null) {

            String idCliente = clienteSelecionado.getId();

            try {

                String data = "{ \"Active\": false }";
                API.updateCliente(idCliente, data);
                Mensagens.Informacao("Cliente Recusado!", "Cliente Recusado com sucesso");

            } catch (IOException e) {
                Mensagens.Erro("Erro!", "Erro ao recusar cliente!");
            }
        }

    }

}

