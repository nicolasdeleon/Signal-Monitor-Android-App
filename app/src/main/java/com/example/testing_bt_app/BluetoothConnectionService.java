package com.example.testing_bt_app;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";

    private static final String appName = "MYAPP";

    private final char STARTER = 0xFF;

    private final int MESSAGE_SIZE = 23;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }


    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {

        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try{
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start.....");

                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            //talk about this is in the 3rd
            if(socket != null){
                connected(socket);
            }

            Log.i(TAG, "END mAcceptThread ");
        }

    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            Log.d("CONNECTING TO: ", mmDevice.getName());
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +MY_UUID_INSECURE );
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE );
            }

            //will talk about this in the 3rd video
            connected(mmSocket);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }



    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    /**
     AcceptThread starts and sits waiting for a connection.
     Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     **/
    public void startClient(BluetoothDevice device,UUID uuid){
        Log.d(TAG, "startClient: Started.");

        //initprogress dialog
        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth"
                ,"Please Wait...",true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    /**
     Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            try{
                mProgressDialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
            }


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public ArrayList<Integer> bytesToInts(byte[] bytes, int length) {
            ArrayList<Integer> res = new ArrayList<>(length);
            for(int i=0; i < bytes.length && i < length; i++) {
                Integer val = 0;
                val = val | (bytes[i] & 0xFF);
                res.add(val);
            }
            return res;
        }

        public void run() {

            int bytes; // bytes returned from read()
            Integer availableBytes = 0;

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream

                // Keep listening to the InputStream until an exception occurs
                try {
                    availableBytes = mmInStream.available();
                    if(availableBytes > MESSAGE_SIZE ) {
                        byte[] buffer = new byte[availableBytes];  // buffer store for the stream
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);
                        Log.d("mmInStream.read(buffer)", buffer.toString());
                        Integer b = new Integer(buffer[0]);
                        if(bytes > 0 && b == -1) {
                            // Send the obtained bytes to the UI activity
                            byte [] buffer2 = new byte[availableBytes];
                            System.arraycopy(buffer, 0, buffer2, 0, availableBytes);

                            byte[] hrCharst1 = new byte[4];
                            byte[] hrCharst2 = new byte[4];
                            byte[] hrCharst3 = new byte[4];
                            byte[] hrCharst4 = new byte[4];
                            byte[] oxyChars = new byte[4];
                            byte[] temp02HrChars = new byte[3];

                            System.arraycopy(buffer2, 1, hrCharst1, 0, 4);
                            System.arraycopy(buffer2, 5, hrCharst2, 0, 4);
                            System.arraycopy(buffer2, 9, hrCharst3, 0, 4);
                            System.arraycopy(buffer2, 13, hrCharst4, 0, 4);
                            System.arraycopy(buffer2, 17, oxyChars, 0, 4);
                            System.arraycopy(buffer2, 21, temp02HrChars, 0, 3);

                            Integer hrValuet1 = ByteBuffer.wrap(hrCharst1).getInt();
                            Integer hrValuet2 = ByteBuffer.wrap(hrCharst2).getInt();
                            Integer hrValuet3 = ByteBuffer.wrap(hrCharst3).getInt();
                            Integer hrValuet4 = ByteBuffer.wrap(hrCharst4).getInt();


                            Integer oxyValue = ByteBuffer.wrap(oxyChars).getInt();

                            ArrayList<Integer> bytes2Ints = bytesToInts(temp02HrChars, 3);

                            bytes2Ints.add(0, hrValuet1);
                            bytes2Ints.add(1, hrValuet2);
                            bytes2Ints.add(2, hrValuet3);
                            bytes2Ints.add(3, hrValuet4);
                            bytes2Ints.add(4, oxyValue);

                            Log.d("hrValuet1", hrValuet1.toString());
                            Log.d("hrValuet2", hrValuet2.toString());
                            Log.d("hrValuet3", hrValuet3.toString());
                            Log.d("hrValuet4", hrValuet4.toString());
                            Log.d("oxyValue", oxyValue.toString());

                            Intent incomingMessageIntent = new Intent("btIncomingMessage");
                            incomingMessageIntent.putIntegerArrayListExtra("btMessage", bytes2Ints);
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);
                        }
                    }
                } catch (IOException e) {
                    Log.d("Error reading", e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to output stream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        // ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }

}
