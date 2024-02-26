package Controller.Encomenda;

import DAL.*;
import Model.*;
import Utilidades.BaseDados;
import Utilidades.Mensagens;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;


/**
 * Classe com funções que leem tabelas de encomendas, linhas das encomendas e aprovam ou recusam as encomendas.
 */
public class AprovarStock {
    private Utilizador utilizador;
    LerEncomenda lerEncomenda = new LerEncomenda();
    LerContaCorrente lerContaCorrente = new LerContaCorrente();
    LerProdutos lerProdutos = new LerProdutos();

    @FXML
    private SplitPane anchorPaneFuncoesFornc;

    @FXML
    private Button btnAprovar;

    @FXML
    private Button btnRecusar;

    @FXML
    public TableView<Encomenda> tableViewEncomendas;

    @FXML
    private TableView<LinhaEncomenda> tableViewLinhasEncomenda;


    public ObservableList<Encomenda> encomendas = FXCollections.observableArrayList();
    ObservableList<LinhaEncomenda> linhasEncomenda = FXCollections.observableArrayList();

    public void iniciaData(Utilizador utilizador) {
        this.utilizador = utilizador;
        System.out.println(utilizador.getEmail());

        System.out.println(utilizador.getTipo());
    }

    /**
     * Inicializa a interface do utilizador e configura a tabela de encomendas pendentes.
     * Adiciona um ouvinte para a seleção de itens na tabela de encomendas para atualizar a tabela de linhas de encomenda.
     *
     * @throws IOException Se ocorrer um erro durante a inicialização.
     */
    public void initialize() throws IOException {

        tabelaEncomendasPendentes();

        tableViewEncomendas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    tabelaLinhasEncomenda(newSelection);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    /**
     * Preenche a tabela de encomendas pendentes com dados da base de dados.
     * Configura as colunas da tabela e associa-as às propriedades da classe Encomenda.
     * Adiciona a tabela à interface do utilizador e exibe as encomendas pendentes.
     *
     * @throws IOException Se ocorrer um erro ao ler a tabela.
     */
    public void tabelaEncomendasPendentes() throws IOException {

        try {
            encomendas.addAll(lerEncomenda.lerEncomendaDaBaseDeDadosPendentes());

            if (!encomendas.isEmpty()) {
                // Defina as colunas da tabela
                TableColumn<Encomenda, Integer> colunaId = new TableColumn<>("ID");
                TableColumn<Encomenda, String> colunaReferencia = new TableColumn<>("Referência");
                TableColumn<Encomenda, LocalDate> colunaData = new TableColumn<>("Data");
                TableColumn<Encomenda, Fornecedor> colunaNomeFornecedor = new TableColumn<>("Fornecedor");
                TableColumn<Encomenda, Fornecedor> colunaIdFornecedor = new TableColumn<>("Id do fornecedor");
                TableColumn<Encomenda, Pais> colunaidPais = new TableColumn<>("País");
                TableColumn<Encomenda, Double> colunaTotalTaxa = new TableColumn<>("Total dos impostos");
                TableColumn<Encomenda, Double> colunaIncidencia = new TableColumn<>("Total sem impostos");
                TableColumn<Encomenda, Double> colunaTotal = new TableColumn<>("Valor total");


                // Associe as colunas às propriedades da classe Encomenda
                colunaId.setCellValueFactory(new PropertyValueFactory<>("id"));
                colunaReferencia.setCellValueFactory(new PropertyValueFactory<>("referencia"));
                colunaData.setCellValueFactory(new PropertyValueFactory<>("data"));

                colunaNomeFornecedor.setCellValueFactory(new PropertyValueFactory<>("fornecedor"));

                colunaNomeFornecedor.setCellFactory(column -> {
                    return new TableCell<Encomenda, Fornecedor>() {
                        @Override
                        protected void updateItem(Fornecedor fornecedor, boolean empty) {
                            super.updateItem(fornecedor, empty);

                            if (fornecedor == null || empty) {
                                setText(null);
                            } else {
                                setText(String.valueOf(fornecedor.getNome())); // Nome do fornecedor
                            }
                        }
                    };
                });

                colunaIdFornecedor.setCellValueFactory(new PropertyValueFactory<>("fornecedor"));

                colunaIdFornecedor.setCellFactory(column -> {
                    return new TableCell<Encomenda, Fornecedor>() {
                        @Override
                        protected void updateItem(Fornecedor fornecedor, boolean empty) {
                            super.updateItem(fornecedor, empty);

                            if (fornecedor == null || empty) {
                                setText(null);
                            } else {
                                setText(String.valueOf(fornecedor.getIdExterno())); // ID do fornecedor
                            }
                        }
                    };
                });

                colunaidPais.setCellValueFactory(new PropertyValueFactory<>("pais"));
                colunaTotalTaxa.setCellValueFactory(new PropertyValueFactory<>("valorImposto"));

                colunaIncidencia.setCellValueFactory(new PropertyValueFactory<>("valorIncidencia"));

                colunaTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));


                // Adicione as colunas à tabela
                tableViewEncomendas.getColumns().add(colunaId);
                tableViewEncomendas.getColumns().add(colunaReferencia);
                tableViewEncomendas.getColumns().add(colunaData);
                tableViewEncomendas.getColumns().add(colunaNomeFornecedor);
                tableViewEncomendas.getColumns().add(colunaIdFornecedor);
                tableViewEncomendas.getColumns().add(colunaidPais);
                tableViewEncomendas.getColumns().add(colunaTotalTaxa);
                tableViewEncomendas.getColumns().add(colunaIncidencia);
                tableViewEncomendas.getColumns().add(colunaTotal);


                tableViewEncomendas.setItems(encomendas);


            }

        } catch (IOException e) {
            Mensagens.Erro("Erro!", "Erro ao ler tabela!");
        }

    }

    /**
     * Preenche a tabela de linhas de encomenda com base na encomenda selecionada.
     * Configura as colunas da tabela e associa-as às propriedades da classe LinhaEncomenda.
     *
     * @param encomenda A encomenda selecionada.
     * @throws IOException Se ocorrer um erro ao ler a tabela.
     */
    public void tabelaLinhasEncomenda(Encomenda encomenda) throws IOException {
        try {

            if (encomenda != null) {

                tableViewLinhasEncomenda.getItems().clear();
                tableViewLinhasEncomenda.getColumns().clear();

                // Ler apenas as linhas de encomenda para a encomenda selecionada
                linhasEncomenda.addAll(lerEncomenda.lerLinhaEncomenda(encomenda.getId()));

                if (!linhasEncomenda.isEmpty()) {
                    TableColumn<LinhaEncomenda, Integer> colunaId = new TableColumn<>("ID");
                    TableColumn<LinhaEncomenda, Encomenda> colunaIdEncomenda = new TableColumn<>("Encomenda");
                    TableColumn<LinhaEncomenda, Integer> colunaSequencia = new TableColumn<>("Sequência");
                    TableColumn<LinhaEncomenda, String> colunaIdProduto = new TableColumn<>("Id do produto");
                    TableColumn<LinhaEncomenda, String> colunaDescricaoProduto = new TableColumn<>("Descrição");
                    TableColumn<LinhaEncomenda, Integer> colunaQuantidade = new TableColumn<>("Quantidade");
                    TableColumn<LinhaEncomenda, String> colunaDescricaoUnidade = new TableColumn<>("Unidade");
                    TableColumn<LinhaEncomenda, String> colunaNomePais = new TableColumn<>("País");
                    TableColumn<LinhaEncomenda, Double> colunaTotalTaxa = new TableColumn<>("Total taxa");
                    TableColumn<LinhaEncomenda, Double> colunaTotalIncidencia = new TableColumn<>("Total incidência");
                    TableColumn<LinhaEncomenda, Double> colunaTotalLinha = new TableColumn<>("Total da linha");

                    colunaId.setCellValueFactory(new PropertyValueFactory<>("id"));
                    colunaIdEncomenda.setCellValueFactory(new PropertyValueFactory<>("IdEncomenda"));

                    colunaIdEncomenda.setCellFactory(column -> {
                        return new TableCell<LinhaEncomenda, Encomenda>() {
                            @Override
                            protected void updateItem(Encomenda encomenda, boolean empty) {
                                super.updateItem(encomenda, empty);

                                if (encomenda == null || empty) {
                                    setText(null);
                                } else {
                                    setText(String.valueOf(encomenda.getId())); // ID do fornecedor
                                }
                            }
                        };
                    });
                    colunaSequencia.setCellValueFactory(new PropertyValueFactory<>("sequencia"));
                    colunaIdProduto.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduto().getId()));
                    colunaDescricaoProduto.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduto().getDescricao()));
                    colunaQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
                    colunaDescricaoUnidade.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduto().getDescricaoUnidade()));
                    colunaNomePais.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTaxa().getNome()));
                    colunaTotalTaxa.setCellValueFactory(new PropertyValueFactory<>("totalTaxa"));
                    colunaTotalIncidencia.setCellValueFactory(new PropertyValueFactory<>("totalIncidencia"));
                    colunaTotalLinha.setCellValueFactory(new PropertyValueFactory<>("total"));

                    tableViewLinhasEncomenda.getColumns().add(colunaId);
                    tableViewLinhasEncomenda.getColumns().add(colunaIdEncomenda);
                    tableViewLinhasEncomenda.getColumns().add(colunaSequencia);
                    tableViewLinhasEncomenda.getColumns().add(colunaIdProduto);
                    tableViewLinhasEncomenda.getColumns().add(colunaDescricaoProduto);
                    tableViewLinhasEncomenda.getColumns().add(colunaQuantidade);
                    tableViewLinhasEncomenda.getColumns().add(colunaDescricaoUnidade);
                    tableViewLinhasEncomenda.getColumns().add(colunaNomePais);
                    tableViewLinhasEncomenda.getColumns().add(colunaTotalTaxa);
                    tableViewLinhasEncomenda.getColumns().add(colunaTotalIncidencia);
                    tableViewLinhasEncomenda.getColumns().add(colunaTotalLinha);

                    tableViewLinhasEncomenda.setItems(linhasEncomenda);

                }

            }
        } catch (Exception e) {
            Mensagens.Erro("Erro!", "Erro ao ler tabela!");
        }
    }

    /**
     * Manipula o evento de clique no botão "Aprovar".
     * Aprova a encomenda, atualiza o stock na base de dados e atualiza o saldo em dívida do fornecedor.
     *
     * @throws IOException Se ocorrer um erro durante o processo de aprovação.
     */
    @FXML
    public void clickAprovar() throws IOException, SQLException {
        iniciaData(utilizador);

        // Aceder a encomenda
        Encomenda encomenda = tableViewEncomendas.getSelectionModel().getSelectedItem();

        if (encomenda != null) {  // Verificar se a encomenda não é nula
            // Aceder as linhas
            List<LinhaEncomenda> linhasEncomenda = lerEncomenda.lerLinhasParaAprovacao(encomenda.getId());

            boolean sucesso = false;
            boolean sucessoEncomenda = false;
            boolean gerarProdutoVenda = false;
            boolean atualizado = false;
            boolean quemAprovou = false;

            double total = encomenda.getValorTotal();

            // Pergunta ao utilizador se deseja enviar produtos para a API
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Enviar para a API");
            alert.setHeaderText("Deseja enviar os produtos para a API também?");
            alert.setContentText("Escolha uma opção:");

            // Adiciona opções ao Alert
            ButtonType buttonTypeYes = new ButtonType("Sim");
            ButtonType buttonTypeNo = new ButtonType("Não");
            ButtonType buttonTypeCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);

            // Obtém a resposta do utilizador
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == buttonTypeYes || result.get() == buttonTypeNo) {
                    for (LinhaEncomenda linhas : linhasEncomenda) {
                        Produto produto = linhas.getProduto();
                        double quantidade = linhas.getQuantidade();
                        double precoUnitario = linhas.getPreco();

                        // Lógica para atualizar o stock na base de dados
                        sucesso = lerEncomenda.atualizarStock(produto.getId(), produto.getUnidade().getId(), quantidade);

                        gerarProdutoVenda = lerProdutos.gerarProdutoParaVenda(produto.getId(), produto.getUnidade().getId(), precoUnitario);

                        tableViewEncomendas.getItems().remove(encomenda);
                    }

                    List<ProdutoVenda> produtosVenda = new ArrayList<>();

                    for (LinhaEncomenda linhas : linhasEncomenda) {
                        produtosVenda.addAll(lerProdutos.obterProdutosVendaDaBaseDadosPorIdProduto(linhas.getProduto().getId()));
                    }

                    for (ProdutoVenda produtoVenda : produtosVenda) {
                        // Obter o preço de venda e a descrição da tabela Preco_venda
                        Map<String, Object> infoProdutoVenda = lerProdutos.obterInfoProdutoVenda(produtoVenda.getProduto().getId());

                        double precoVenda = (double) infoProdutoVenda.get("precoVenda");
                        String descricao = (String) infoProdutoVenda.get("descricao");

                        Map<String, Object> informacoesStock = lerProdutos.obterInformacoesDoStock(produtoVenda.getProduto().getId(), produtoVenda.getUnidade().getId());

                        // Acesse as informações com base nas chaves do Map
                        double quantidadeStock = (double) informacoesStock.get("quantidadeStock");
                        String descricaoUnidade = (String) informacoesStock.get("descricaoUnidade");

                        // Verifica se o utilizador clicou em "Sim" antes de enviar para a API
                        if (result.get() == buttonTypeYes) {
                            boolean api = lerProdutos.enviarProdutosParaAPI(produtoVenda, descricao, descricaoUnidade, quantidadeStock, precoVenda);
                        }
                    }
                }

                //atualizar estado da encomenda
                sucessoEncomenda = lerEncomenda.atualizarEstadoEncomenda(encomenda.getId());

                //atualizar saldo em dívida
                atualizado = lerContaCorrente.atualizarSaldoDevedores(total, encomenda.getFornecedor().getIdExterno());

                //Guarda informação de quem aprovou a encomenda
                if (utilizador != null) {
                    quemAprovou = lerEncomenda.quemAprovouEncomenda(encomenda.getId(), utilizador.getId());
                }

                // Listener para seleção de encomendas
                tableViewEncomendas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        try {
                            // Quando uma nova encomenda é selecionada, atualize a tabela de linhas
                            tabelaLinhasEncomenda(newSelection);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                if (tableViewEncomendas.getItems().isEmpty()) {
                    tableViewLinhasEncomenda.getItems().clear();
                }

                if (sucesso && sucessoEncomenda && atualizado && quemAprovou) {
                    Mensagens.Informacao("Sucesso", "Stock aprovado com sucesso!");
                } else {
                    Mensagens.Erro("Erro!", "Erro ao atualizar stock!");
                }
            }
        }
    }




    /**
     * Manipula o evento de clique no botão "Recusar".
     * Recusa a encomenda e atualiza o estado da encomenda na base de dados.
     *
     * @throws IOException Se ocorrer um erro durante o processo de recusa.
     */
    @FXML
    void clickRecusar() throws IOException {
        iniciaData(utilizador);
        // Aceder a encomenda
        Encomenda encomenda = tableViewEncomendas.getSelectionModel().getSelectedItem();

        //atualizar estado da encomenda para recusada (Estado 2)
        boolean sucessoEncomenda = lerEncomenda.actualizarEstadoEncomendaRecusada(encomenda.getId());


        //Guarda informação de quem aprovou a encomenda
        boolean quemAprovou = false;
        if (utilizador != null) {
            quemAprovou = lerEncomenda.quemAprovouEncomenda(encomenda.getId(), utilizador.getId());
        }

        // Remover encomenda da tabela apresentada
        tableViewEncomendas.getItems().remove(encomenda);

        // Listener para seleção de encomendas
        tableViewEncomendas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    // Quando uma nova encomenda é selecionada, atualize a tabela de linhas
                    tabelaLinhasEncomenda(newSelection);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        if (tableViewEncomendas.getItems().isEmpty()) {
            tableViewLinhasEncomenda.getItems().clear();
        }


        if (sucessoEncomenda && quemAprovou) {
            Mensagens.Informacao("Sucesso", "Encomenda recusada com sucesso!");
        } else {
            Mensagens.Erro("Erro!", "Erro ao recusar encomenda!");
        }

    }

}
