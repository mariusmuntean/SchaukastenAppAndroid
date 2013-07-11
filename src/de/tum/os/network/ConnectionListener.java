package de.tum.os.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;

import de.tum.os.activities.models.ICommandExecuter;
import de.tum.os.sa.shared.Command;

/**
 * Created by Marius on 7/11/13.
 */
public class ConnectionListener {
    private boolean go = true;

    Socket socket;
    String serverIP = "131.159.193.227";
    int serverPort = 3535;
    ObjectInputStream ois;
    ICommandExecuter commandExecuter;

    public ConnectionListener(ICommandExecuter commandExecuter) {
        this.commandExecuter = commandExecuter;
    }


    public void StartListening() {
        this.go = true;

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(serverIP, serverPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (go) {
                    try {
                        ois = new ObjectInputStream(socket.getInputStream());
                        Command command = (Command) ois.readObject();
                        commandExecuter.ExecuteCommand(command);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t = new Thread(r);
        t.start();

    }

    public void StopListening() {
        this.go = false;
    }
}
