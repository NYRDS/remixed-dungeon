//Please use SClientLua.java instead of this.

//Optimized for work with .NET Server. (https://github.com/anunknowperson/Simple-server)
//Coded by Sergey Kiselev in 2020
//Licensed by MIT License.

package com.nyrds.pixeldungeon.networking;

import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SClient {

    public static final String TAG = SClient.class.getSimpleName();
    public static String SERVER_IP; //server IP address
    public static int SERVER_PORT;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private DataOutputStream mBufferOut;
    // used to read messages from the server
    private DataInputStream mBufferIn;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public SClient(OnMessageReceived listener, String ip, int port) {
        SERVER_IP = ip;
        SERVER_PORT = port;

        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    try{
                        Log.d(TAG, "Sending: " + message);

                        byte mData[] = message.getBytes("UTF-8"); //Get message data in bytes
                        int mLenght = (int) mData.length; //We need send lenght of our message
                        byte mLenghtData[] = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(mLenght).array(); //.NET Has another byte order in reading integer. We need to flip bytes before sending.

                        mBufferOut.write(mLenghtData); //Write lenght
                        mBufferOut.write(mData); //Write data
                    } catch (Exception e){
                        Log.e("TCP", "S: Error", e);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            try{
                mBufferOut.flush();
                mBufferOut.close();
            } catch (Exception e){

            }
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.d("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVER_PORT);

            try {

                //sends the message to the server
                mBufferOut = new DataOutputStream(socket.getOutputStream());

                //receives the message which the server sends back
                mBufferIn = new DataInputStream(socket.getInputStream());


                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    byte mLenghtData[] = new byte[4]; //MessageLenght native byte data
                    mBufferIn.read(mLenghtData, 0, 4);

                    int mLenght = ByteBuffer.wrap(mLenghtData).order(ByteOrder.LITTLE_ENDIAN).getInt(); //Message lenght

                    byte mData[] = new byte[mLenght];
                    mBufferIn.read(mData, 0, mLenght);

                    mServerMessage = new String(mData, "UTF-8");

                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                    }
                }

                Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        void messageReceived(String message);
    }

}
