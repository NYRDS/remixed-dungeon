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

                        //.NET Has another byte order in reading integer. We need to flip bytes before sending.
                        ByteBuffer temp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
                        byte mLenghtData[] = temp.putInt(mLenght).array();

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
                Log.e("TCP", "S: Error", e);
            }
        }

        mMessageListener = null;
        mBufferIn = null;

        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {
        mRun = true;

        Socket socket = null;

        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            Log.d("TCP Client", "C: Connecting...");
            socket = new Socket(serverAddr, SERVER_PORT);

                mBufferOut = new DataOutputStream(socket.getOutputStream());
                mBufferIn = new DataInputStream(socket.getInputStream());

                while (mRun) {
                    byte mLenghtData[] = new byte[4]; //MessageLenght native byte data
                    mBufferIn.read(mLenghtData, 0, 4);

                    int mLenght = ByteBuffer.wrap(mLenghtData).order(ByteOrder.LITTLE_ENDIAN).getInt(); //Message lenght
                    byte mData[] = new byte[mLenght];

                    mBufferIn.read(mData, 0, mLenght);
                    mServerMessage = new String(mData, "UTF-8");

                    if (mServerMessage != null && mMessageListener != null) {
                        mMessageListener.messageReceived(mServerMessage); //call the method messageReceived from MyActivity class
                    }
                }
                Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                try{
                    socket.close();
                } catch (Exception e){
                    Log.e("TCP_Closing", "S: Error", e);
                }
            }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        void messageReceived(String message);
    }

}
