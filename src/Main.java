import connection.ConnectionManager;
import downloads.DownloadTaskManager;
import files.FileManager;
import gui.Gui;
import requests.PeerRequestManager;

import java.io.File;

public class Main {

    public static void main(String[] args) {

        if (2 != args.length) {
            System.err.println("Please pass two arguments to the command. " +
                    "First is the Connection port and second is your file containing directory.");
            return;
        }

        int connectionPort;
        try {
            connectionPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            System.err.println("Port number is not in the correct format. Please use a correct port number.");
            return;
        }

        String fileDirectoryName = args[1];
        File dir = new File(fileDirectoryName);

        if(!dir.exists() || !dir.isDirectory()) {
            System.err.println("Directory "+fileDirectoryName+" is not a valid directory for " +
                    "path = "+dir.getAbsolutePath()+". Cannot proceed.");
            return;
        }

        ConnectionManager.createInstance("127.0.0.1", connectionPort);
        ConnectionManager.getInstance().start();

        FileManager.createInstance(dir);
        PeerRequestManager.createInstance();
        DownloadTaskManager.createInstance();

        Gui.createInstance("127.0.0.1", connectionPort);
        Gui.getInstance().open();
    }

}
