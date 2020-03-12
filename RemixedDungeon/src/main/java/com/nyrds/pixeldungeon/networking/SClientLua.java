//Class for easy pairing SClient with lua
//Please use this instead of SClient.java
//You can do everything with this server
//Sample code:
//Initializing
//client = new SClientLua("<server_ip>", <port>).connect();
//Sending messages
//client.sendMessage(input.getText().toString());
//Receiving messages (for example, you can do this for each frame in lua code)
//if (client.canReceive()) output.setText(output.getText().toString() + client.receiveMessage() + "\n");

//You can also use SClientLua.anunknowip and SClientLua.anunknownport to connect to http://www.anunknown.site/ (but only if you name is Bogdan Kiykov)

//Optimized for work with .NET Server (https://github.com/anunknowperson/Simple-server)

//Coded by Sergey Kiselev in 2020
//Licensed by MIT License.

//Класс для простой связки Lua и SClient.java
//Пожалуйста, используй это вместо SClient.java
//Ты можешь делать всё что угодно с этим клиентом. Всё в твоих руках.
//Примеры:
//Инициализация и подключение
//client = new SClientLua("<server_ip>", <port>).connect();
//Отправка сообщений
//client.sendMessage(input.getText().toString());
//Получение сообщений (for example, you can do this for each frame in lua code)
//if (client.canReceive()) output.setText(output.getText().toString() + client.receiveMessage() + "\n");

//Вы также можете использовать SClientLua.anunknowip и SClientLua.anunknownport, чтобы подключиться к http://www.anunknown.site/ (Но только если вы - Богдан Кийков))

//Оптимизированно для работы с сервером .NET (https://github.com/anunknowperson/Simple-server)

//Создано Сергеем Киселёвым в 2020
//Лицензировано лицензией MIT.

package com.nyrds.pixeldungeon.networking;

import android.os.AsyncTask;
import java.util.ArrayList;

public class SClientLua {
    public static final String anunknownip = "37.194.195.213";
    public static final int    anunknownport = 3002;

    private String ip;
    private int port;
    private SClient mTcpClient;
    private ArrayList<String> buffer = new ArrayList<>();

    public SClientLua(String f_ip, int f_port){
        ip = f_ip;
        port = f_port;
    }

    public static SClientLua createNew(String ip, int port){ return new SClientLua(ip, port); } //Function for lua...

    public SClientLua connect(){ //Connect to server
        new SClientTask().execute("");

        return this;
    }

    public void sendMessage(String message){ //Send message to server
        mTcpClient.sendMessage(message);
    }

    public String receiveMessage(){ //Get message from buffer
        String message = buffer.get(0);

        buffer.remove(0);

        return message;
    }

    public boolean canReceive(){ return (buffer.size() == 0 ? false : true); }

    public class SClientTask extends AsyncTask<String, String, SClient> {
        @Override
        protected SClient doInBackground(String... message) {
            //we create a TCPClient object
            mTcpClient = new SClient(new SClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(final String message) {
                    buffer.add(message);
                }
            }, ip, port);

            mTcpClient.run();

            return null;
        }
    }
}
