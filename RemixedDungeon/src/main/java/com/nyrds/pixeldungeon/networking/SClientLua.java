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

import com.nyrds.LuaInterface;

import java.util.concurrent.ConcurrentLinkedQueue;

@LuaInterface
public class SClientLua {
    private String ip;
    private int port;
    private SClient mTcpClient;
    private ConcurrentLinkedQueue<String> buffer = new ConcurrentLinkedQueue<>();

    public SClientLua(String f_ip, int f_port){
        ip = f_ip;
        port = f_port;
    }

    @LuaInterface
    public static SClientLua createNew(String ip, int port){ return new SClientLua(ip, port); } //Function for lua...

    public SClientLua connect(){ //Connect to server
        mTcpClient = new SClient(msg -> buffer.add(msg), ip, port);

        new SClientTask().execute("");

        while (!mTcpClient.isInitialized.get()){}

        return this;
    }

    public void stop(){mTcpClient.stopClient();}

    public void sendMessage(String message){ //Send message to server
        mTcpClient.sendMessage(message);
    }

    public String receiveMessage(){ //Get message from buffer
        return buffer.poll();
    }

    public boolean canReceive(){ return (buffer.size() != 0); }

    public class SClientTask extends AsyncTask<String, String, SClient> {
        @Override
        protected SClient doInBackground(String... message) {
            mTcpClient.run();

            return null;
        }
    }
}
