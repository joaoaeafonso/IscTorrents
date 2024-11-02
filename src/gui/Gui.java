package gui;

import connection.ConnectionManager;
import connection.messages.NewConnectionRequest;
import connection.models.MessageType;
import connection.models.PeerInformation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import static utils.TorrentsUtils.generateIdentifier;

public class Gui {

    private final JFrame frame;

    private JButton searchButton;
    private JButton downloadButton;
    private JButton connectButton;

    private JList<String> resultList;
    private DefaultListModel<String> listModel;

    public Gui(String address, int port, File fileDir) {
        frame = new JFrame("Port Node Address[address="+address+", port="+port+"]");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        addContent();
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

        JTextField searchField = new JTextField("");
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

            ConnectionManager instance = ConnectionManager.getInstance();

            NewConnectionRequest request = new NewConnectionRequest(instance.getInformation(), MessageType.CONNECTION_REQUEST);
            PeerInformation newConnectInfo = new PeerInformation(address, convertedPort, generateIdentifier(address, convertedPort));

            instance.sendMessage(newConnectInfo, request);
        }
    }

    private void downloadFile() {
        //TODO(joaoaeafonso): This is to me removed. For testing purposes only
        String selectedItem = resultList.getSelectedValue();
        if (selectedItem != null) {
            JOptionPane.showMessageDialog(null, "Descarregando: " + selectedItem);
        } else {
            JOptionPane.showMessageDialog(null, "Por favor, selecione um item da lista.");
        }
    }

    private void searchFile() {
        //TODO(joaoaeafonso): This is to me removed. For testing purposes only
        listModel.clear();
        listModel.addElement("Resultado 1");
        listModel.addElement("Resultado 2");
        listModel.addElement("Resultado 3");
    }

}
