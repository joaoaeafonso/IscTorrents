package gui;

import common.Pair;
import connection.ConnectionManager;
import connection.messages.FileBlockRequestMessage;
import connection.models.PeerInformation;
import files.models.FileSearchResult;
import requests.PeerRequestManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
    private List<FileSearchResult> displayedFilesInformation;

    private static Gui instance;

    private Gui(String address, int port) {
        frame = new JFrame("Port Node Address[address="+address+", port="+port+"]");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.displayedResultsMap = new HashMap<>();
        this.displayedFilesInformation = new ArrayList<>();

        addContent();
    }

    public static synchronized Gui getInstance() {
        return instance;
    }

    public static synchronized void createInstance(String address, int port) {
        if( null == instance ) {
            instance = new Gui(address, port);
        }
    }

    public void open() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(dimension.width / 2, dimension.height / 2);
        frame.setVisible(true);
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
        searchButton.addActionListener(_ -> searchFile());
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
        new Thread( () -> {
            String selectedItem = resultList.getSelectedValue();
            if(selectedItem != null) {
                PeerRequestManager.getInstance().peerDownloadRequest(getFileDownloadInformation(selectedItem));
            }
        } ).start();
    }

    public void handleDownloadFinished(ConcurrentHashMap<PeerInformation, AtomicInteger> downloadProviders, long downloadDurationSec) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Download Successfully finished\n\n");

        messageBuilder.append("Messages received per peer:\n");
        for(Map.Entry<PeerInformation, AtomicInteger> entry: downloadProviders.entrySet()) {
            messageBuilder.append("Peer [")
                    .append(entry.getKey())
                    .append("], Number of File Blocks [")
                    .append(entry.getValue().get())
                    .append("] \n");
        }

        messageBuilder.append("\nTotal download time: ").append(downloadDurationSec).append(" seconds");
        JOptionPane.showMessageDialog(null, messageBuilder.toString(), "Download Successfully finished", JOptionPane.INFORMATION_MESSAGE);
    }

    private Pair<FileSearchResult, List<PeerInformation>> getFileDownloadInformation(String fileName) {
        int index = fileName.indexOf(" ");
        if (index != -1) {
            fileName = fileName.substring(0, index);
        }

        FileSearchResult downloadInformation = null;
        List<PeerInformation> fileHolders = new ArrayList<>();
        for(FileSearchResult searchResult: displayedFilesInformation) {
            if(Objects.equals(searchResult.getFileName(), fileName)) {
                downloadInformation = searchResult;
                fileHolders.add(searchResult.getPeerInformation());
            }
        }

        return new Pair<>(downloadInformation, fileHolders);
    }

    private void searchFile() {
        String searchText = searchField.getText();
        PeerRequestManager.getInstance().peerFileWordSearchRequest(ConnectionManager.getInstance().getAllConnectedPeers(), searchText);
    }

    public synchronized void updateResultList(List<FileSearchResult> fileSearchResults) {
        if( null == fileSearchResults || fileSearchResults.isEmpty() ) {
            listModel.clear();
            displayedResultsMap.clear();
            return;
        }

        listModel.clear();
        displayedResultsMap.clear();
        displayedFilesInformation.clear();

        for(FileSearchResult file: fileSearchResults) {
            displayedResultsMap.put(file.getFileName(), displayedResultsMap.getOrDefault(file.getFileName(), 0) + 1);
        }

        for(Map.Entry<String, Integer> entry : displayedResultsMap.entrySet()){
            String resultEntry = entry.getKey() + " <"+entry.getValue().toString()+">";
            this.listModel.addElement(resultEntry);
        }

        displayedFilesInformation = new ArrayList<>(fileSearchResults);
    }

}
