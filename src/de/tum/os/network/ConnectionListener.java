package de.tum.os.network;

import java.io.ObjectInputStream;
import java.net.Socket;

import de.tum.os.models.ICommandExecuter;
import de.tum.os.sa.shared.commands.Command;

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

    public ConnectionListener(Socket socket, ICommandExecuter commandExecuter) throws Exception {
        if(socket == null || socket.isClosed() || commandExecuter == null){
            throw new Exception("Arguments cannot be null");
        }
        this.commandExecuter = commandExecuter;
        this.socket = socket;
    }


    public void StartListening() {
        this.go = true;

        Runnable r = new Runnable() {
            @Override
            public void run() {
//                try {
//                    socket = new Socket(serverIP, serverPort);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                try {
                    ois = new ObjectInputStream(socket.getInputStream());
                    while (go) {
                        Command command = (Command) ois.readObject();
                        commandExecuter.ExecuteCommand(command);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
