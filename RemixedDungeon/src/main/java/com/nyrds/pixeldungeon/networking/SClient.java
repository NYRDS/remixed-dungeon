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
import java.util.concurrent.atomic.AtomicBoolean;

public class SClient {

    public static final String TAG = SClient.class.getSimpleName();
    public static String SERVER_IP; //server IP address
    public static int SERVER_PORT;

    public AtomicBoolean isInitialized = new AtomicBoolean(false);
    // sends message received notifications
    private OnMessageReceived mMessageListener;
    // while this is true, the server will continue running
    private AtomicBoolean mRun = new AtomicBoolean(false);
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
        Runnable runnable = () -> sendMessageSc(message);
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun.set(false);

        if (mBufferOut != null) {
            try {
                mBufferOut.flush();
                mBufferOut.close();
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            }
        }

        mMessageListener = null;
        mBufferIn = null;

        mBufferOut = null;
    }

    public void run() {
        String mServerMessage;

        mRun.set(true);

        Socket socket = null;

        while (mRun.get()){
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket(serverAddr, SERVER_PORT);

                mBufferOut = new DataOutputStream(socket.getOutputStream());
                mBufferIn = new DataInputStream(socket.getInputStream());

                isInitialized.set(true);

                while (mRun.get()) {
                    byte[] lengthData = new byte[4]; //MessageLength native byte data
                    mBufferIn.read(lengthData, 0, 4);

                    int len = arrtoint(lengthData);
                    byte inData[] = new byte[len];

                    mBufferIn.read(inData, 0, len);
                    mServerMessage = new String(inData, "UTF-8");

                    receiveMessage(mServerMessage);
                }
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
    }

    private int arrtoint(byte data[]){
        return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private void receiveMessage(String mServerMessage){
        if (mServerMessage != null && mMessageListener != null) {
            mMessageListener.messageReceived(mServerMessage); //call the method messageReceived from MyActivity class
        }
    }

    private void sendMessageSc(String message){
        if (mBufferOut != null) {
            try{
                Log.d(TAG, "Sending: " + message);

                byte[] data = message.getBytes("UTF-8"); //Get message data in bytes
                int length = data.length; //We need send length of our message

                //.NET Has another byte order in reading integer. We need to flip bytes before sending.
                ByteBuffer temp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
                byte[] lenData = temp.putInt(length).array();

                mBufferOut.write(lenData); //Write length
                mBufferOut.write(data); //Write data
            } catch (Exception e){
                Log.e("TCP", "S: Error", e);
            }
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        void messageReceived(String message);
    }

}
