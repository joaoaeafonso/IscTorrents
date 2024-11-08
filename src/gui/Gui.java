package gui;

import connection.ConnectionManager;
import connection.models.PeerInformation;
import files.models.FileSearchResult;
import requests.PeerRequestManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.TorrentsUtils.generateIdentifier;

public class Gui {

    private final JFrame frame;

    private JButton searchButton;
    private JButton downloadButton;
    private JButton connectButton;

    private JTextField searchField;

    private JList<String> resultList;
    private DefaultListModel<String> listModel;
    private final Map<String, Integer> displayedResultsMap;

    private static Gui instance;

    private Gui(String address, int port, File fileDir) {
        frame = new JFrame("Port Node Address[address="+address+", port="+port+"]");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.displayedResultsMap = new HashMap<>();

        addContent();
    }

    public static synchronized Gui getInstance() {
        return instance;
    }

    public static synchronized void createInstance(String address, int port, File fileDir) {
        if( null == instance ) {
            instance = new Gui(address, port, fileDir);
        }
    }

    public void open() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(dimension.width / 2, dimension.height / 2);
        frame.setVisible(true);
    }

    public synchronized void updateResultList(List<FileSearchResult> fileSearchResults) {
        if( null == fileSearchResults ) {
            return;
        }

        listModel.clear();
        displayedResultsMap.clear();

        for(FileSearchResult file: fileSearchResults) {
            if( displayedResultsMap.containsKey(file.getFileName()) ) {
                displayedResultsMap.compute(file.getFileName(), (_, numPeersWithFile) -> numPeersWithFile + 1);
                continue;
            }

            displayedResultsMap.put(file.getFileName(), 1);
        }

        for(Map.Entry<String, Integer> entry : displayedResultsMap.entrySet()){
            String resultEntry = entry.getKey() + " <"+entry.getValue().toString()+">";
            this.listModel.addElement(resultEntry);
        }
    }

    private void addContent() {
        addSearchPanel();
        addMainPanel();
        addEvents();
    }

    private void addMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(resultList);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1));
        downloadButton = new JButton("Descarregar");
        connectButton = new JButton("Ligar a Nó");

        buttonPanel.add(downloadButton);
        buttonPanel.add(connectButton);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        frame.add(mainPanel, BorderLayout.CENTER);
    }

    private void addSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());

        JLabel searchLabel = new JLabel();
        searchLabel.setText("Texto a procurar:");

        searchField = new JTextField("");
        searchButton = new JButton("Procurar");

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        frame.add(searchPanel, BorderLayout.NORTH);
    }

    private void addEvents() {
        searchButton.addActionListener(e -> searchFile());
        downloadButton.addActionListener(_ -> downloadFile());
        connectButton.addActionListener(_ -> connectToPeerNode());
    }

    private void connectToPeerNode() {
        JTextField addressField = new JTextField("127.0.0.1");
        JTextField portField = new JTextField("8082");

        Object[] message = {
                "Endereço:", addressField,
                "Porta:", portField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String address = addressField.getText();
            String port = portField.getText();

            int convertedPort = Integer.parseInt(port);

            PeerInformation newConnectInfo = new PeerInformation(address, convertedPort, generateIdentifier(address, convertedPort));
            PeerRequestManager.getInstance().peerConnectionRequest(newConnectInfo);
        }
    }

    private void downloadFile() {
        //TODO(joaoaeafonso): This is to be removed. For testing purposes only
        String selectedItem = resultList.getSelectedValue();
        if (selectedItem != null) {
            JOptionPane.showMessageDialog(null, "Descarregando: " + selectedItem);
        } else {
            JOptionPane.showMessageDialog(null, "Por favor, selecione um item da lista.");
        }
    }

    private void searchFile() {
        String searchText = searchField.getText();
        PeerRequestManager.getInstance().peerFileWordSearchRequest(ConnectionManager.getInstance().getAllConnectedPeers(), searchText);
    }

}
