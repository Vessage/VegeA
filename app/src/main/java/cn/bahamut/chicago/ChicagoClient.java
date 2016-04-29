package cn.bahamut.chicago;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import cn.bahamut.common.BitUtil;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observable;

/**
 * Created by alexchow on 16/4/29.
 */
public class ChicagoClient extends Observable {
    protected Socket client;
    private byte[] receiveBuffer = new byte[16 * 1024];
    private volatile boolean _isRunning = false;
    private final int TCP_PACKAGE_HEAD_SIZE = 4;

    private InetSocketAddress remoteAddress;

    private Thread receiveThread;

    protected HashMap<String, Object> HandlerKeyMap;

    public IDeserializeMessage messageDepressor;

    private Queue<byte[]> sendMessageQueue = new ConcurrentLinkedQueue<>();

    public ChicagoClient(IDeserializeMessage MessageDepressor) {
        this.messageDepressor = MessageDepressor;
        initClient();
    }

    private void initClient() {
        HandlerKeyMap = new HashMap<>();
    }

    public void setBufferSize(int NewLength) {
        receiveBuffer = new byte[NewLength];
    }

    public void addHandlerCallback(String ExtensionName, Object Command, EventHandler<ClientEventArgs> Callback) {
        Object key = generateKey(ExtensionName, Command);
    }

    private Object generateKey(String ExtensionName, Object Command) {
        String cmd = generateCmdValue(Command);
        String key = generateCmdKey(ExtensionName, cmd);
        if (HandlerKeyMap.containsKey(key)) {
            return HandlerKeyMap.get(key);
        } else {
            HandlerKeyMap.put(key, key);
        }
        return key;
    }

    private String generateCmdKey(String ExtensionName, String cmdValue) {
        String key = String.format("On%s_%s", ExtensionName, cmdValue);
        return key;
    }

    private String generateCmdValue(Object Command) {
        String cmd;
        if (Command instanceof Integer) {
            cmd = String.format("CmdId(%d)", Command);
        } else {
            cmd = Command.toString();
        }
        return cmd;
    }

    public boolean isRunning() {
        return _isRunning;
    }

    protected void setRunning(boolean isRunning) {
        _isRunning = _isRunning;
    }

    public void start(InetSocketAddress address, int Port) {
        remoteAddress = address;
        Thread startThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client = new Socket();
                    _isRunning = false;
                    client.connect(remoteAddress);
                    setRunning(true);
                    receiveThread = new Thread(receiveProc);
                    receiveThread.start();
                    startSendQueue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        startThread.start();
    }

    private void startSendQueue() {
        try {
            OutputStream outputStream = client.getOutputStream();
            while (isRunning()) {
                byte[] sendMsg;
                while ((sendMsg = sendMessageQueue.peek()) != null) {
                    outputStream.write(sendMsg, 0, sendMsg.length);
                    outputStream.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable receiveProc = new Runnable() {
        @Override
        public void run() {
            try {
                InputStream inputStream = client.getInputStream();
                while (inputStream.read(receiveBuffer, 0, 4) == TCP_PACKAGE_HEAD_SIZE) {
                    int packageLength = BitUtil.byteArrayToInt(receiveBuffer, 0);
                    if (packageLength > 0) {
                        if (inputStream.read(receiveBuffer, 0, packageLength) == packageLength) {
                            ClientBaseMessage msg = messageDepressor.getMessageFromBuffer(receiveBuffer, packageLength);
                            ClientEventArgs args = new ClientEventArgs();
                            args.setState(msg);

                            Object handlerKey = null;
                            if (StringHelper.isStringNullOrEmpty(msg.getCommandName())) {
                                handlerKey = generateKey(msg.getExtension(), msg.getCommandId());
                            } else {
                                handlerKey = generateKey(msg.getExtension(), msg.getCommandName());
                            }
                            //Object eventHandler = Events[handlerKey];
                            //EventHandler<ClientEventArgs> handler = eventHandler as EventHandler<ClientEventArgs>;
                            //if (handler != null)
                            //{
                            //    DispatcherEvent(handler, args);
                            //}
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    protected void DispatcherEvent(EventHandler<ClientEventArgs> handler, ClientEventArgs args) {
        if (handler == null) return;
        Object[] param = new Object[]{this, args};
    }

//    protected void ConnectCallback(IAsyncResult ar)
//    {
//        try
//        {
//
//            ReceiveHead();
//            DispatchClientConnected();
//        }
//        catch (Exception ex)
//        {
//            DispatchSendFailed(ex, "Remote Server Not Response");
//            DispatchClientDisconnected();
//            return;
//        }
//    }


    public void sendMessageAsync(byte[] Data, int Length) {
        byte[] sendData = new byte[Data.length + 4];
        int len = BitUtil.CreateDataPackageWithHead(sendData, Data, Length);
        sendMessageQueue.add(sendData);
    }


    private void DispatchClientConnected() {

    }

    private void DispatchClientDisconnected() {

    }

    private void DispatchSendFailed(Exception ex, String Message) {

    }


    public void Close() {
//        if (IsRunning && client != null && client.Connected)
//        {
//            IsRunning = false;
//            DispatchClientDisconnected();
//            client.Close();
//        }
    }

    public void Dispose() {
        Close();
        //Events.Dispose();
    }


}
