package com.apps.dave.davepluxandroid;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import plux.android.bioplux.BPException;
import plux.android.bioplux.Device;

import static com.apps.dave.davepluxandroid.R.id.action_off;
import static com.apps.dave.davepluxandroid.R.id.action_on;
import static com.apps.dave.davepluxandroid.R.layout.activity_main;

//import com.bitalino.comm.BITalinoDevice;

//import info.plux.pluxapi.bitalino.BITalinoFrame;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String remoteMAC = "00:07:80:D8:AB:52";
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;


    protected static final int DEVICE_TYPE_UNKNOWN = 0;
    protected static final int DEVICE_TYPE_CLASSIC = 1;
    protected static final int DEVICE_TYPE_LE = 2;
    protected static final int DEVICE_TYPE_DUAL = 3;

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private String mConnectedDeviceName = null;
    private StringBuffer mOutStringBuffer;
    private BluetoothChatService mChatService = null;






    public final static String EXTRA_DEVICE = "info.plux.pluxapi.sampleapp.DeviceActivity.EXTRA_DEVICE";
    public final static String FRAME = "info.plux.pluxapi.sampleapp.DeviceActivity.Frame";

    private boolean bolBroacastRegistred;

    ArrayList<BluetoothDevice> bleDevices;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice dev = null;
    BluetoothSocket sock= null;
    IntentFilter filter;
    //Handler mHandler;

    BluetoothDevice device;


//    DeviceTest myd = null;
//    DeviceBluetooth myDevice = null;
    Device myDevice;



    private InputStream is = null;
    private OutputStream os = null;
    private  static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final long SCAN_PERIOD = 5000;

    TextView action_result;
    TextView action_description;
    TextView action_error;

    private GraphView g1;

    private RadioButton radioConn;
    private RadioButton radioPlux;
    private RadioButton radioAcq;
    private RadioButton radioTest;


    private Button next;


    boolean isBitalano = false;
    boolean testMode = true;
    boolean isOn = false;
    boolean isPaired = false;
    boolean isAquiring = false;
    boolean isInTestMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initView();
        permissionCheck();
    }



    private void initView(){
        action_result = (TextView) findViewById(R.id.action_result);
        action_description = (TextView) findViewById(R.id.action_description);
        action_error = (TextView) findViewById(R.id.action_error);
        radioConn = (RadioButton) findViewById(R.id.radioBluetooth);
        radioConn.setChecked(false);
        radioPlux = (RadioButton) findViewById(R.id.pluxConnect);
        radioPlux.setChecked(false);
        radioAcq  = (RadioButton) findViewById(R.id.radioAquire);
        radioAcq.setChecked(false);

        radioTest = (RadioButton) findViewById(R.id.test_mode);
        radioTest.setChecked(true);
        g1 =(GraphView) findViewById(R.id.g1);
        initDevice("test");
        isInTestMode = true;


        next = (Button) findViewById(R.id.action_next);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getApplicationContext(),GraphActivity.class);
                //startActivity(intent);
                Log.d(TAG,"Clicked ..... ");
                //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                //registerReceiver(mReceiver, filter);
                String message = "R";
                sendMessage(message);
            }
        });

       //checkIfBluetoothisOn();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            Log.d(TAG, "Bluetooth not Supported: ");
        }else {
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(TAG,"Bluetoot not Enabled asking .....");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
                radioConn.setChecked(false);
                isOn = false;
            } else {
                radioConn.setChecked(true);
                isOn = true;
            }
        }

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mReceiver,filter);

        //connectDevice();

    }
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Log.d(TAG, "Not Connected");
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            Log.d(TAG,">>>><<<<<<"+String.valueOf(send[0]));

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            action_result.setText(mOutStringBuffer);
        }
    }
    private void connectDevice() {
        Log.d(TAG,"Connect to device chat");

        device = mBluetoothAdapter.getRemoteDevice(remoteMAC);
        // Attempt to connect to the device
        boolean secure = false;
        mChatService.connect(device, secure);
    }

    private void disConnectDevice() {
        Log.d(TAG,"Dis-Connect to device chat");
        radioPlux.setChecked(false);
        mChatService.stop();

    }
    private void getDeviceName(){
        action_result.setText("Message\n");
        Log.d(TAG,"get device name");
        byte[] data;
        mChatService.write("V".getBytes());

    }

    private void beginBAcq(){
        int frequency =1000;
        int channelMask = 255;
        int numberBits = 12;
        int channelNumber = 0;

        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        String frequencyString = "0000" + frequency;
        String channelString = "00" + Integer.toHexString(channelMask).toUpperCase();
        String numberBitsString = "00" + numberBits;

        for (int i = 0; i < 8; i++)
        {
            if ((channelMask & 0x1) == 1)
            {
                sb2.append(i + 1);
                sb2.append(" ");
                channelNumber = ((byte)(channelNumber + 1));
            }
            channelMask >>= 1;
        }
        sb.append("@START,");
        sb.append(frequencyString.substring(frequencyString.length() - 4, frequencyString.length()));
        sb.append(",");
        sb.append(channelString.substring(channelString.length() - 2, channelString.length()));
        sb.append(",");
        sb.append(numberBitsString.substring(numberBitsString.length() - 2, numberBitsString.length()));
        sb.append(";");


        mChatService.write(sb.toString().getBytes());


        //setNumberOfChannels(sour);

       //command = ((CommandProperties)a.a.getCommand(commandArguments)).command;








    }
    private void endBAcq (){

//        List<Source> sour = new ArrayList<>();
//        Source emgSource = new Source(1,16,(byte)0x01,100);
//        Source inertial = new Source(2,16,(byte)0xeF,100);
//
//        sour.add(emgSource);
//        sour.add(inertial);
//        CommandArguments commandArguments;
//        (commandArguments = new CommandArguments()).setBaseFreq(1000);
//        commandArguments.setSources(sour);
//
//        Log.d(TAG,"Port "+String.valueOf(emgSource.getPort()));
//        Log.d(TAG,"Channel Mask "+String.valueOf(emgSource.getChannelMask()));
//        Log.d(TAG,"nBits "+String.valueOf(emgSource.getnBits()));


//        (commandArguments = new CommandArguments()).setBaseFreq(1000);
//        commandArguments.setSources(sour);

        try {
//            CommandArguments command = new CommandArguments();
//            commandArguments.setBatteryThreshold(value);

            // byte[] comm = ((CommandProperties)BITalino.BATTERY.getCommand(commandArguments)).command;

            byte[] comm = BITalino.STOP.toString().getBytes();
            Log.d(TAG,"Command get State"+String.valueOf(comm));
            //comm = "V".getBytes();
            mChatService.write(comm);
        }catch (NullPointerException e){
            Log.d(TAG,"Null point Exception");
        }
        //mChatService.write("R".getBytes());

    }


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {



            switch (msg.what) {

                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Log.d(TAG,"state connected");
                            radioPlux.setChecked(true);
                            getDeviceName();
                            break;

                        case BluetoothChatService.STATE_CONNECTING:
                            Log.d(TAG,"state Connecting ....");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                            Log.d(TAG,"state Listen");
                            break;
                        case BluetoothChatService.STATE_NONE:
                            Log.d(TAG,"state none");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    Log.d(TAG,"MESSAGE_WRITE");
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.d(TAG,"WM  >"+writeMessage);
                   // mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG,"RM >>>>"+readMessage);
                    action_result.append(readMessage);
                   // mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    Log.d(TAG,"MESSAGE_DEVICE_NAME");
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    action_description.setText("");
                    action_description.setText(mConnectedDeviceName);

                    break;
                case Constants.MESSAGE_TOAST:
                    String efromchat = msg.getData().getString(Constants.TOAST);
                    Log.d(TAG, "Message Toast >>"+efromchat);
                    action_error.setText(efromchat);

                    break;
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG," Broacast Action   >>>>>>>>>>>>>>>>>>>"+action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                Log.d(TAG,"Action_Found");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG,deviceName+" : "+deviceHardwareAddress);
                action_result.append(deviceName+"\n");
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                Log.d(TAG,"Discovery Started");
                radioAcq.setChecked(true);
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                radioAcq.setChecked(false);
                Log.d(TAG,"Discovery Finished");
            }
            if(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)){
                Log.d(TAG,"ACTION_CONNECTION_STATE_CHANGED");

            }
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                Log.d(TAG,"Bluetooth state Changed");
                checkIfBluetoothisOn();
            }
    }
    };


    private   void setup(){
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if(mBluetoothAdapter == null){
//            Log.d(TAG, "Bluetooth not Supported: ");
//        }else{
//        }
    }

    private void checkIfBluetoothisOn(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            Log.d(TAG, "Bluetooth not Supported: ");
        }else {

            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(TAG,"Bluetoot not Enabled asking .....");
                //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
                radioConn.setChecked(false);
                isOn = false;
            } else {
                radioConn.setChecked(true);
                isOn = true;
            }
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        MenuItem action_on  = menu.findItem(R.id.action_on);
        MenuItem action_off = menu.findItem(R.id.action_off);

        MenuItem action_paired = menu.findItem(R.id.action_paired);
        MenuItem action_newpaired = menu.findItem(R.id.action_newpaired);
        MenuItem action_unpaired  = menu.findItem(R.id.action_unpaired);

        MenuItem beginAqu = menu.findItem(R.id.beginAqu);
        MenuItem endAqu = menu.findItem(R.id.endAqu);

        MenuItem close = menu.findItem(R.id.close);
        MenuItem checkDevice = menu.findItem(R.id.check_mac);

        MenuItem inTestMode = menu.findItem(R.id.test_mode);
        MenuItem inRealMode = menu.findItem(R.id.real_mode);

        //reset errors on new menu choice
        action_error.setText("");

        if(isOn){
            action_on.setVisible(false);
            action_off.setVisible(true);
        }else{
            action_on.setVisible(true);
            action_off.setVisible(false);
        }
        if(isPaired){
            action_newpaired.setVisible(false);
            action_unpaired.setVisible(true);

        }else{
            action_newpaired.setVisible(true);
            action_unpaired.setVisible(false);

        }
        if(isAquiring){
            beginAqu.setVisible(false);
            endAqu.setVisible(true);
        }else{
            beginAqu.setVisible(true);
            endAqu.setVisible(false);
        }

        if(isInTestMode){
            inTestMode.setVisible(false);
            inRealMode.setVisible(true);
        }else{
            inTestMode.setVisible(true);
            inRealMode.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();



        if (id == action_off) {
            Toast.makeText(getApplicationContext(), "BLE Turn OFF :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "BLE Off - ");
            turnOffBT();
            isOn = false;
            return true;
        }
        if (id == action_on) {
            Toast.makeText(getApplicationContext(), "BLE Turn ON :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "BLE ON - ");
            turnOnBT();
            isOn = true;
            return true;
        }

        if (id == R.id.action_discover) {
            Toast.makeText(getApplicationContext(), "Discover :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Discover - ");
            //TODO Add discovery method
            action_result.setText("Discovered\n");
            //connectDevice();
            if(mBluetoothAdapter == null){
                return true;
            }
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();

            return true;
        }
        if (id == R.id.action_paired) {
            Toast.makeText(getApplicationContext(), "Paired Devices:", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Paired Devices- ");
            findPaired();

            return true;
        }
        if (id == R.id.action_newpaired) {
            Toast.makeText(getApplicationContext(), "Paired :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Pair again - ");
            pairDevice(dev);
            isPaired = true;

            return true;
        }
        if (id == R.id.action_unpaired) {
            Toast.makeText(getApplicationContext(), "Unpair :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "un Pair - ");
            unpairDevice(dev);
            isPaired = false;

            return true;
        }
        if (id == R.id.beginAqu) {
            Toast.makeText(getApplicationContext(), "Begin Aqu :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Begin Aquisition - ");
            isAquiring = true;

            // TODO CONNECT TO DEVICE

            beginBAcq();
//            try {
//                myDevice.BeginAcq();
//                radioAcq.setChecked(true);
//
//            } catch (BPException e) {
//                e.printStackTrace();
//                radioAcq.setChecked(false);
//            }catch(NullPointerException e){
//                e.printStackTrace();
//                radioAcq.setChecked(false);
//            }

            //new NewAsyncTask ().execute();

            return true;
        }
        if (id == R.id.endAqu) {
            Toast.makeText(getApplicationContext(), "End Aqu :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "End Aqu- ");
            isAquiring = false;
            // TODO DISCONNECT TO DEVICE
            endBAcq ();
           // pause(500);



//            try {
//                myDevice.EndAcq();
//                radioAcq.setChecked(false);
//            } catch (BPException e) {
//                e.printStackTrace();
//                Log.d(TAG,"End Acqerror");
//                action_description.setText("");
//                action_description.setText(e.toString());
//            }


            return true;
        }

        if (id == R.id.read_frames) {
            Toast.makeText(getApplicationContext(), "Bitalo - Start:", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Read Frames - ");
            action_result.setText("");
            g1.removeAllSeries();
            //TODO move to AsyncTask
            startRecording();
            return true;
        }

        if (id == R.id.close) {
            Toast.makeText(getApplicationContext(), "Close -", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Close   - ");
            action_description.setText("");
            disConnectDevice();
            try {
                myDevice.Close();
            } catch (BPException e) {
                e.printStackTrace();
                Log.d(TAG, "Close  error BPException - ");
            }catch(NullPointerException e){
                e.printStackTrace();
                Log.d(TAG, "Close  error NullPointException - ");
            }
            return true;
        }
        if (id == R.id.check_mac) {
            Toast.makeText(getApplicationContext(), "Device Version -:", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Device Version - ");
            action_description.setText("");
            connectDevice();
//            try {
//                String ans = myDevice.GetDescription();
//
//                Log.d(TAG,"Device  :"+ans);
//                action_description.setText(ans);
//
//            } catch (BPException e) {
//                e.printStackTrace();
//                Log.d(TAG,"get description error");
//                action_description.setText(e.toString());
//            } catch (NullPointerException e){
//                e.printStackTrace();
//                Log.d(TAG,"get description error");
//                // action_description.setText(e.toString());
//            }

            return true;
        }
        if (id == R.id.test_mode) {
            Toast.makeText(getApplicationContext(), "Test Mode :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Test Mode ON - ");
            isInTestMode = true;
            radioTest.setChecked(true);
            g1.removeAllSeries();
            try {
                myDevice.Close();
            } catch (BPException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
                action_error.setText(e.getMessage());
            }
            try {
                myDevice = Device.Create("test");
            } catch (BPException e) {
                e.printStackTrace();
                action_error.setText(e.getMessage());
            }
            try {
                String ans = myDevice.GetDescription();
                Log.d(TAG,"Device  :"+ans);
                action_description.setText("");
                action_description.setText(ans);
            } catch (BPException e) {
                e.printStackTrace();
                Log.d(TAG,"get description error");
                action_error.setText(e.toString());
            } catch (NullPointerException e){
                e.printStackTrace();
                Log.d(TAG,"get description error");
               action_error.setText(e.toString());
            }

            return true;
        }
        if (id == R.id.real_mode) {
            Toast.makeText(getApplicationContext(), "Real Time Mode:", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Real Time Mode - ");
            isInTestMode = false;

            g1.removeAllSeries();
            radioTest.setChecked(false);
            try {
                myDevice.Close();
            } catch (BPException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
                action_error.setText(e.getMessage());
            }
            try {
                myDevice = Device.Create(remoteMAC);
            } catch (BPException e) {
                e.printStackTrace();
                action_error.setText(e.getMessage());
            }

            try {
                String ans = myDevice.GetDescription();
                Log.d(TAG,"Device  :"+ans);
                action_description.setText("");
                action_description.setText(ans);
            } catch (BPException e) {
                e.printStackTrace();
                Log.d(TAG,"get description error");
                action_error.setText(e.toString());
            } catch (NullPointerException e){
                e.printStackTrace();
                Log.d(TAG,"get description error");
                action_error.setText(e.toString());
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ////////////////////// LifeCycle Events  ////////////////////////////////////////

    @Override
    protected void onPause() {
        super.onPause();

    }
    @Override
    protected void onStart() {
        Log.d("Lifecycle >>>>>>","OnStart");
        super.onStart();
    }
    @Override
    protected void onRestart() {
        Log.d("Lifecycle >>>>>>","OnReStart");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.d("Lifecycle >>>>>>","OnDestroy");
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    protected void onStop() {
        Log.d("Lifecycle >>>>>>","OnStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                //mChatService.start();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
       // mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

       // mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
       // mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
//        mSendButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // Send a message using content of the edit text widget
//                View view = getView();
//                if (null != view) {
//                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
//                    String message = textView.getText().toString();
//                    sendMessage(message);
//                }
//            }
//        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    public void turnOnBT() {
        Log.d(TAG, "Turning On Bluetooth ..: ");
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "Bluetooth not Enabled: ");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent,REQUEST_ENABLE_BT);
            //radioConn.setChecked(true);
        }else{
            // Toast.makeText(getApplicationContext(), "Bluetooth is Enabled...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Bluetooth Enabled: ");
           // radioConn.setChecked(true);
        }
    }

    // Turn Off Bluetooth
    public void turnOffBT(){
        Toast.makeText(getApplicationContext(), "Turning off Bluetooth", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Turning Off Bluetooth: ");
        if (mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Bluetooth turning off...", Toast.LENGTH_SHORT).show();
            mBluetoothAdapter.disable();
            //radioConn.setChecked(false);
        }else{
            Toast.makeText(getApplicationContext(), "Bluetooth is off...", Toast.LENGTH_SHORT).show();
            //radioConn.setChecked(false);
        }
    }
    //Check valid BLE Address
    public boolean checkValidAddress(String Add){
        //check if valid mac address
        return mBluetoothAdapter.checkBluetoothAddress (Add);
    }

    //Start Discovery
    public void startDiscovery() {
        // TODO Auto-generated method stub
        if(mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter.startDiscovery();
        }


    }
    //Pair Device
    public void pairDevice(BluetoothDevice device) {
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "Bluetooth Not Enabled...");
            return;
        }
        action_description.setText("");
        try {
            Log.d(TAG, "Start Pairing...");
            boolean waitingForBonding = true;
            Method m = device.getClass()
                    .getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d(TAG, "Pairing finished.");

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        try{
            int s = device.getType();
            String ans = device.getName();
            Log.d(TAG,ans);
            String a = getBluetoothType(s);
            Log.d(TAG,a);
            action_description.setText(ans+ " : "+a);

        }catch (Exception e){
            Log.e(TAG, e.getMessage());

        }
        int secs = 2; // Delay in seconds

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                Log.d(TAG,"Running code after 5 sec");
                findPaired();
            }
        }, 5000);

    }
    //Unpair Device
    public void unpairDevice(BluetoothDevice device) {
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "Bluetooth Not Enabled...");
            return;
        }
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                Log.d(TAG,"Running code after 5 sec");
                findPaired();
            }
        }, 5000);

    }
    //Find Paired Devices
    public void findPaired() {

        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth Not Enabled...");
            return;
        }
        action_result.setText("Paired\n");
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, deviceName + ": " + deviceHardwareAddress);
                action_result.append(deviceHardwareAddress +"\n");
            }
        } else {

            Log.d(TAG, "No Paired Devices: ");
        }
    }
    public String getBluetoothType(int t){
        String ans;

        if(t == DEVICE_TYPE_CLASSIC){
            ans = "DEVICE_TYPE_CLASSIC";

        }else if(t == DEVICE_TYPE_LE){
            ans = "DEVICE_TYPE_LE";

        }else if(t == DEVICE_TYPE_DUAL){
            ans = "DEVICE_TYPE_DUAL";

        }else{
            ans = "DEVICE_TYPE_UNKNOWN";

        }
        return ans;
    }

    public void initDevice(String mac){
        action_error.setText("");
        try {
            myDevice = Device.Create(mac);
        } catch (BPException e) {
            e.printStackTrace();
            Log.d(TAG,"Create error "+e.getMessage());
            action_error.setText(e.getMessage());
        }catch(NullPointerException e){
            Log.d(TAG,"Null Point Exception");
        }
        String name = "";

        try {
            name = myDevice.GetDescription();
        } catch (BPException e) {
            e.printStackTrace();
            Log.d(TAG,"Get Description error "+e.getMessage());
            action_error.setText(e.getMessage());
        }catch(NullPointerException e){
            Log.d(TAG,"Null Point Exception");

        }
        action_description.setText(name);

    }
    public void startRecording(){

        int nFrames = 127;
        int [] arrayA = new int[nFrames];
        short [] arrayB = new short[nFrames];
        short [] arrayC = new short[nFrames];
        short [] arrayD = new short[nFrames];
        short [] arrayE = new short[nFrames];



        try {
            myDevice.BeginAcq(nFrames,255,12);
        } catch (BPException e) {
            e.printStackTrace();
            Log.d(TAG,"Begin Aqu error ");
            action_error.setText(e.getMessage());
        }

        Device.Frame[] frames = new Device.Frame[nFrames];
        for(int i = 0; i < frames.length; i++) {
            frames[i] = new Device.Frame();
        }
        pause(50);

        try {
            myDevice.GetFrames(nFrames,frames);
        } catch (BPException e) {
            e.printStackTrace();
            Log.d(TAG,"Get Frames error ");
            action_error.setText(e.getMessage());
        }

        try {
            myDevice.EndAcq();
        } catch (BPException e) {
            e.printStackTrace();
            Log.d(TAG,"End Aqu error ");
            action_error.setText(e.getMessage());
        }

        String result;

        for(int i = 0; i < frames.length; i++){
            result = frames[i].seq+" : "+frames[i].an_in[0]+" , "+frames[i].an_in[1];
            Log.d(TAG,result);
            arrayA[i]= frames[i].seq;
            arrayB[i] = frames[i].an_in[0];
            arrayC[i] = frames[i].an_in[1];
            arrayD[i] = frames[i].an_in[2];
            arrayE[i] = frames[i].an_in[3];
        }

        graphTestData(arrayA, arrayB, arrayC, arrayD, arrayE, 1);
        graphTestData(arrayA, arrayB, arrayC, arrayD, arrayE, 2);
        graphTestData(arrayA, arrayB, arrayC, arrayD, arrayE, 3);
        graphTestData(arrayA, arrayB, arrayC, arrayD, arrayE, 4);
    }

    public void graphTestData(int[] arrayA, short[] arrayB, short[] arrayC, short[] arrayD, short[] arrayE,int ch){
        //"series"+ch;

        if(ch == 1) {
            LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>(gererateTestPoints1(arrayA,arrayB));
            series1.setColor(Color.GREEN);
            g1.addSeries(series1);
        }else if(ch == 2){
            LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(gererateTestPoints2(arrayA,arrayC));
            series2.setColor(Color.RED);
            g1.addSeries(series2);

        }
        else if(ch == 3){
            LineGraphSeries<DataPoint> series3 = new LineGraphSeries<>(gererateTestPoints3(arrayA,arrayD));
            series3.setColor(Color.YELLOW);
            g1.addSeries(series3);

        }
        else if(ch == 4){
            LineGraphSeries<DataPoint> series4= new LineGraphSeries<>(gererateTestPoints4(arrayA,arrayE));
            g1.addSeries(series4);
            series4.setColor(Color.BLUE);
        }

        g1.getViewport().setXAxisBoundsManual(true);
        g1.getViewport().setMinX(0);
        g1.getViewport().setMaxX(127);
    }
    private DataPoint[] gererateTestPoints1(int [] arrayA, short[] arrayB) {

        DataPoint[] values = new DataPoint[arrayA.length];
        for (int i=0; i< arrayB.length; i++) {
            double x = arrayA[i];
            double y =arrayB[i];
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }
    private DataPoint[] gererateTestPoints2(int [] arrayA, short[] arrayC) {

        DataPoint[] values = new DataPoint[arrayA.length];
        for (int i=0; i< arrayA.length; i++) {
            double x = arrayA[i];
            double y =arrayC[i];
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }
    private DataPoint[] gererateTestPoints3(int [] arrayA, short[] arrayD) {

        DataPoint[] values = new DataPoint[arrayA.length];
        for (int i=0; i< arrayA.length; i++) {
            double x = arrayA[i];
            double y =arrayD[i];
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }
    private DataPoint[] gererateTestPoints4(int [] arrayA, short[] arrayE) {

        DataPoint[] values = new DataPoint[arrayA.length];
        for (int i=0; i< arrayA.length; i++) {
            double x = arrayA[i];
            double y =arrayE[i];
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }

    public short[] getData(){
        String str = "";
        int nFrames = 10;
        Device.Frame myFrame = new Device.Frame();

        Device.Frame[] nyFrameArray = new Device.Frame[nFrames];
        for(int i=0; i < nFrames; i++){
            nyFrameArray[i]=myFrame;
        }


        try {
            myDevice.GetFrames(nFrames,nyFrameArray);
        } catch (BPException e) {
            e.printStackTrace();
        }

        return nyFrameArray[0].an_in;

    }

    private int[] convertToBitalinoChannelsArray(
            ArrayList<Integer> activeChannels) {
        int[] activeChannelsArray = new int[activeChannels.size()];
        Iterator<Integer> iterator = activeChannels.iterator();
        Log.e(TAG, "BITALINO ActiveChannels ");

        for (int i = 0; i < activeChannelsArray.length; i++) {
            activeChannelsArray[i] = iterator.next().intValue()-1;
            Log.e(TAG, "BITALINO ActiveChannels C" + activeChannelsArray[i]);
        }

        return activeChannelsArray;
    }





    public void pause(int time){
        try {
            Thread.sleep(time);
        } catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"pause error ...");
        }
    }

    //TODO Thread functions

    private String[] getBluetoothDevices(){
        String[] result = null;
        ArrayList<String> devices = new ArrayList<String>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()){
            Log.e("Dialog", "Couldn't find enabled the mBluetoothAdapter");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            Set<BluetoothDevice> devList = mBluetoothAdapter.getBondedDevices();

            for( BluetoothDevice device : devList)
                devices.add(device.getName() + "-"+ device.getAddress());

            String[] aux_items = new String[devices.size()];
            final String[] items = devices.toArray(aux_items);
            result = items;
        }
        return result;

    }

///////////////////////////////////////////////////////////////////

    private void permissionCheck() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            Log.d(TAG,"SDK >= 23 ...");
            List<String> permissionsNeeded = new ArrayList();
            List<String> permissionsList = new ArrayList();
            if (!addPermission(permissionsList, "android.permission.ACCESS_COARSE_LOCATION")) {
                //permissionsNeeded.add("Bluetooth Scan");
                Log.d(TAG,"permissionsNeeded Bluetooth Scan ...");
            }
            if (!addPermission(permissionsList, "android.permission.READ_EXTERNAL_STORAGE")) {
                //permissionsNeeded.add("Read");
                Log.d(TAG,"permissionsNeeded Read ...");
            }
            if (!addPermission(permissionsList, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                //permissionsNeeded.add("Write in Storage");
                Log.d(TAG,"permissionsNeeded write external storage ...");
            }
            //if (checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != 0) {
                //Builder builder = new Builder(this);
                //builder.setTitle(getString(C0224R.string.permission_check_dialog_title)).setMessage(getString(C0224R.string.permission_check_dialog_message)).setPositiveButton(getString(C0224R.string.permission_check_dialog_positive_button), null).setOnDismissListener(new C02411(permissionsList));
                //builder.show();
            //}
        }else{
            Log.d(TAG,"SDK < 23 ...");
        }


    }

    @TargetApi(23)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            if (!shouldShowRequestPermissionRationale(permission)) {
                return false;
            }
        }
        return true;
    }


    public  boolean connectBitalino(String add){
        boolean b = false;

//        try {
//
//            bitalino.connect(add);
//            b = true;
//        } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
//            e.printStackTrace();
//            Log.d(TAG,"Bitalano connection error ^^^^^^^^^^^^");
//        }
        return b;
    }
    public void disconnectBitalino(){

//        try {
//            bitalino.disconnect();
//        } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
//            e.printStackTrace();
//            Log.d(TAG,"disconnect Bitalano error ...");
//        }
    }

    public void  readFromFrames(int nF ,int ch) {

        short[] datas = new short[nF];
        for (int i = 0; i < nF; i++) {
            String result = Arrays.toString(getData());
            datas[i] = getData()[ch];
            //Log.d(TAG,getData()[1] + "");

        }
        graphData(datas,ch);
    }



    public void graphData(short[] arrayB,int ch){
       //"series"+ch;

        if(ch == 1) {
            LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>(gererateDataPoints(arrayB));
            series1.setColor(Color.GREEN);
            g1.addSeries(series1);
        }else if(ch == 2){
            LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(gererateDataPoints(arrayB));
            series2.setColor(Color.RED);
            g1.addSeries(series2);

        }
        else if(ch == 3){
            LineGraphSeries<DataPoint> series3 = new LineGraphSeries<>(gererateDataPoints(arrayB));
            series3.setColor(Color.YELLOW);
            g1.addSeries(series3);

        }
        else if(ch == 4){
            LineGraphSeries<DataPoint> series4= new LineGraphSeries<>(gererateDataPoints(arrayB));
            g1.addSeries(series4);
            series4.setColor(Color.BLUE);
        }

        g1.getViewport().setXAxisBoundsManual(true);
        g1.getViewport().setMinX(0);
        g1.getViewport().setMaxX(40);
    }
    private DataPoint[] gererateDataPoints(short[] arrayB) {

        DataPoint[] values = new DataPoint[arrayB.length];
        for (int i=0; i< arrayB.length; i++) {
            double x = i;
            double y =arrayB[i];
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }


    public void getMenu(){

        AlertDialog.Builder popDialog = new AlertDialog.Builder(MainActivity.this);
        View editText = new EditText(MainActivity.this);
        int width = 150;
        int height = -1;
        float gravity = 1.0f;
        editText.setLayoutParams(new Toolbar.LayoutParams(width,height,(int) gravity));
        popDialog.setTitle("The Title");
        popDialog.setMessage((CharSequence) "Click Yes To Exit app");
        popDialog.setView(editText);
        popDialog.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {

                MainActivity.this.finish();
            }
        });
        popDialog.setNegativeButton("No",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {

                dialog.cancel();
            }
        });

        popDialog.setCancelable(true);
        popDialog.create();
        popDialog.show();


    }

}
