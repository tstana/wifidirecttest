package com.example.wifidirecttest;

import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by thedi on 03.06.15.
 */
public class ClientTask extends AsyncTask<Void, Void, String>
{
    private TextView textStatus;
    private InetAddress serverAdress;

    private static final int PORT = 1234;

    public ClientTask(View textStatus, InetAddress serverAdress)
    {
        this.textStatus = (TextView) textStatus;
        this.serverAdress = serverAdress;
    }

    @Override
    protected String doInBackground(Void... params)
    {
        String rcvd = null;
        Socket socket;
        BufferedReader reader;
        PrintWriter writer;

        /* Create client socket and write some data to it */
        try {
            socket = new Socket(serverAdress, PORT);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            rcvd = reader.readLine();
            if (rcvd.equals("comply")) {
                writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("ack");
                writer.close();
            }
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rcvd;
    }

    @Override
    protected void onPostExecute(String result)
    {
        setTextStatus("CLI: " + result);
    }

    private void setTextStatus(String string)
    {
        String tmp = String.valueOf(textStatus.getText());
        tmp += string;
        tmp += "\n";
        textStatus.setText(tmp);
    }
}
