package com.example.wifidirecttest;

import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by thedi on 03.06.15.
 */
public class ServerTask extends AsyncTask<Void, Void, String>
{
    private TextView textStatus;

    private static final int PORT = 1234;

    public ServerTask(View textStatus)
    {
        this.textStatus = (TextView) textStatus;
    }

    @Override
    protected String doInBackground(Void... params)
    {
        ServerSocket serverSocket;
        Socket socket;
        PrintWriter writer;
        BufferedReader reader;
        String reply = null;

        /**
         * Create a server socket and wait for client connections. This
         * call blocks until a connection is accepted from a client
         */
        try {
            serverSocket = new ServerSocket(PORT);
            socket = serverSocket.accept();

            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("comply");

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            reply = reader.readLine();

            reader.close();
            writer.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return reply;
    }

    @Override
    protected void onPostExecute(String result)
    {
        setTextStatus("SRV: " + result);
    }

    private void setTextStatus(String string)
    {
        String tmp = String.valueOf(textStatus.getText());
        tmp += string;
        tmp += "\n";
        textStatus.setText(tmp);
    }
}
