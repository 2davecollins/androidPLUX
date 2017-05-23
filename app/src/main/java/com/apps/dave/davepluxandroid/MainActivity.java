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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
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

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import info.plux.pluxapi.BTHDeviceScan;
import info.plux.pluxapi.Communication;
import info.plux.pluxapi.Constants;
import info.plux.pluxapi.bitalino.BITalinoCommunication;
import info.plux.pluxapi.bitalino.BITalinoCommunicationFactory;
import info.plux.pluxapi.bitalino.BITalinoDescription;
import info.plux.pluxapi.bitalino.BITalinoFrame;
import info.plux.pluxapi.bitalino.BITalinoState;

import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;
import static com.apps.dave.davepluxandroid.R.layout.activity_main;
import static info.plux.pluxapi.Constants.ACTION_COMMAND_REPLY;
import static info.plux.pluxapi.Constants.ACTION_DATA_AVAILABLE;
import static info.plux.pluxapi.Constants.ACTION_DEVICE_READY;
import static info.plux.pluxapi.Constants.ACTION_EVENT_AVAILABLE;
import static info.plux.pluxapi.Constants.EXTRA_COMMAND_REPLY;
import static info.plux.pluxapi.Constants.EXTRA_DATA;
import static info.plux.pluxapi.Constants.EXTRA_STATE_CHANGED;
import static info.plux.pluxapi.Constants.IDENTIFIER;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String remoteMAC = "00:07:80:D8:AB:52";
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    private int REQUEST_ENABLE_BT = 1;

    protected static final int DEVICE_TYPE_UNKNOWN = 0;
    protected static final int DEVICE_TYPE_CLASSIC = 1;
    protected static final int DEVICE_TYPE_LE = 2;
    protected static final int DEVICE_TYPE_DUAL = 3;

    public final static String EXTRA_DEVICE = "info.plux.pluxapi.sampleapp.DeviceActivity.EXTRA_DEVICE";
    public final static String FRAME = "info.plux.pluxapi.sampleapp.DeviceActivity.Frame";

    private boolean bolBroacastRegistred;

    ArrayList<BluetoothDevice> bleDevices;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice dev = null;
    BluetoothSocket sock= null;
    IntentFilter filter;
    Handler mHandler;

    private Handler handler;

    private BTHDeviceScan bthDeviceScan;
    private boolean mScanning;
    private BITalinoCommunication bitalino;
    private BITalinoCommunication bitalin;

    private BluetoothSocket sock1 = null;
    private InputStream is = null;
    private OutputStream os = null;
    private BITalinoDevice bit;

    private  static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final long SCAN_PERIOD = 10000;

    TextView action_result;
    TextView action_description;

    private RadioButton radioConn;
    private Button next;

    MyBluetooth myB;
    boolean isBitalano = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setup();
        initView();
        permissionCheck();

    }
    private final BroadcastReceiver pluxReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            String identifier;
            Log.d("PluxReceiver  *******>"," action "+action);
            if (Constants.ACTION_STATE_CHANGED.equals(action)) {
                identifier = intent.getStringExtra(IDENTIFIER);
                Constants.States state = Constants.States.getStates(intent.getIntExtra(EXTRA_STATE_CHANGED,0));
                Log.i(TAG, "Device " + identifier + ": " + state.name());
                action_result.setText(state.name());
                if(state.name().equals("DISCONNECTED")){
                    radioConn.setChecked(false);
                    Log.d(TAG," disConnected   >>>>>>>>");
                }else if(state.name().equals("CONNECTED")){
                    radioConn.setChecked(true);
                    Log.d(TAG," Connected   >>>>>>>>");
                }else{


                }
            } else if (Constants.ACTION_MESSAGE_SCAN.equals(action)) {
                Log.d("PluxReceiver  >>>>", "ACTION_MESSAGE_SCAN" );

            }
            else if (Constants.ACTION_CONNECTED.equals(action)) {
                Log.d("PluxReceiver  >>>>", "ACTION_CONNECTED" );

            }
            else if (Constants.ACTION_DISCONNECTED.equals(action)) {
                Log.d("PluxReceiver  >>>>", "ACTION_DISCONNECTED" );

            }
            else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d("PluxReceiver  >>>>", "ACTION_DATA_AVAILABLE" );

            }
            else if (Constants.ACTION_DEVICE_READY.equals(action)) {
                Log.d("PluxReceiver  >>>>", "ACTION_DEVICE_READY" );

            }
            else if (Constants.ACTION_EVENT_AVAILABLE.equals(action)) {
                Log.d("PluxReceiver  >>>>", "ACTION_MESSAGE_SCAN" );

            }
            else if (Constants.ACTION_COMMAND_REPLY.equals(action)) {
                Log.d("PluxReceiver  >>>>", "ACTION_COMMAND_REPLY" );

            }
            else if (Constants.ACTION_LOG_AVAILABLE.equals(action)) {
                Log.d("PluxReceiver  >>>>", "ACTION_LOG_AVAILABLE" );

            }

            /*
            Constants

            ACTION_STATE_CHANGED = "info.plux.pluxapi.ACTION_STATE_CHANGED";
            ACTION_CONNECTED = "info.plux.pluxapi.ACTION_CONNECTED";
            ACTION_DISCONNECTED = "info.plux.pluxapi.ACTION_DISCONNECTED";
            ACTION_GATT_SERVICES_DISCOVERED = "info.plux.pluxapi.ACTION_GATT_SERVICES_DISCOVERED";
            ACTION_DATA_AVAILABLE = "info.plux.pluxapi.ACTION_DATA_AVAILABLE";
            ACTION_DEVICE_READY = "info.plux.pluxapi.ACTION_DEVICE";
            ACTION_MESSAGE_SCAN = "info.plux.pluxapi.ACTION_MESSAGE_SCAN";
            ACTION_EVENT_AVAILABLE = "info.plux.pluxapi.ACTION_EVENT_AVAILABLE";
            ACTION_COMMAND_REPLY = "info.plux.pluxapi.ACTION_COMMAND_REPLY";
            ACTION_LOG_AVAILABLE = "info.plux.pluxapi.ACTION_LOG_AVAILABLE";
            UPDATE_TIME = "info.plux.pluxapi.UPDATE_TIME";
            EXTRA_STATE_CHANGED = "info.plux.pluxapi.EXTRA_STATE_CHANGED";
            EXTRA_DEVICE_SCAN = "info.plux.pluxapi.EXTRA_DEVICE_SCAN";
            EXTRA_DESCRIPTION = "info.plux.pluxapi.EXTRA_DESCRIPTION";
            EXTRA_DATA = "info.plux.pluxapi.EXTRA_DATA";
            EXTRA_EVENT = "info.plux.pluxapi.EXTRA_EVENT";
            EXTRA_COMMAND_REPLY = "info.plux.pluxapi.EXTRA_COMMAND_REPLY";
            EXTRA_LOG = "info.plux.pluxapi.EXTRA_LOG";
            EXTRA_TIME = "info.plux.pluxapi.EXTRA_TIME";
            PLUX_DEVICE = "info.plux.pluxapi.PLUX_DEVICE";
            BATTERY_EVENT = "info.plux.pluxapi.BATTERY_EVENT";
            ON_BODY_EVENT = "info.plux.pluxapi.ON_BODY_EVENT";
            DISCONNECT_EVENT = "info.plux.pluxapi.DISCONNECT_EVENT";
            IDENTIFIER = "info.plux.pluxapi.IDENTIFIER";

            */

        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (Constants.ACTION_STATE_CHANGED.equals(action)) {
                String identifier = intent.getStringExtra(IDENTIFIER);
                Constants.States state = Constants.States.getStates(intent.getIntExtra(EXTRA_STATE_CHANGED,0));
                Log.i(TAG, "Device " + identifier + ": " + state.name());
                action_result.setText(state.name());
                if(state.name().equals("DISCONNECTED")){
                    radioConn.setChecked(false);
                    Log.d(TAG," disConnected   >>>>>>>>");
                    //cleanup();
                    //stopBitalino();

                }else if(state.name().equals("CONNECTED")){
                    radioConn.setChecked(true);
                    Log.d(TAG," Connected   >>>>>>>>");

                    try {
                        sock1 = dev.createRfcommSocketToServiceRecord(MY_UUID);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG," IO Exception   >>>>>>>>");
                    }
//                    try {
//                        sock1.connect();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        Log.d(TAG," IO Exception   >>>>>>>>");
//                    }

//                    startBitalino();
//                    try {
//                        bit.open(sock1.getInputStream(),sock1.getOutputStream());
//                    } catch (BITalinoException e) {
//                        e.printStackTrace();
//                        Log.d(TAG," BITalinoException exc   >>>>>>>>");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        Log.d(TAG," IO Exception   >>>>>>>>");
//                    }

                    int counter = 0;

//                    while (counter <100){
//                        final int numSamples = 1000;
//                        Log.d(TAG, "Reading   :"+counter);
//
//                        com.bitalino.comm.BITalinoFrame[] fram = new com.bitalino.comm.BITalinoFrame[0];
//                        try {
//                            fram = bit.read(numSamples);
//                        } catch (BITalinoException e) {
//                            e.printStackTrace();
//                            Log.d(TAG,"BIT exp");
//                        }
//                        for(com.bitalino.comm.BITalinoFrame frame: fram){
//
//                            Log.d(TAG,frame.toString());
//
//                        }
//
//
//                        counter ++;
//
//                    }
                    Log.d(TAG, "Reading Finished  :");

                }else{


                }
            } else if (ACTION_DATA_AVAILABLE.equals(action)) {
                // BITalinoFrame frames = intent.getParcelableExtra(Constants.EXTRA_DATA);

                Log.d(TAG, "Action Data Available >>>>>>>>>>>  " );
                if(intent.hasExtra(EXTRA_DATA)){
                    Parcelable parcelable = intent.getParcelableExtra(EXTRA_DATA);
                    if(parcelable.getClass().equals(BITalinoFrame.class)){ //BITalino
                        action_result.setText(parcelable.toString());
                        Log.d(TAG, ">>>>>>>>>>>  "+parcelable.toString() );
                    }
                }

            } else if (ACTION_COMMAND_REPLY.equals(action)) {

                String identifier = intent.getStringExtra(IDENTIFIER);
                Log.d(TAG,"Action Command Reply >>>>>>>>>>>>>>>>>>  " + identifier);

                if(intent.hasExtra(EXTRA_COMMAND_REPLY) && (intent.getParcelableExtra(EXTRA_COMMAND_REPLY) != null)) {
                    Parcelable parcelable = intent.getParcelableExtra(EXTRA_COMMAND_REPLY);
                    Log.d(TAG,">>>>>>>>>>>>>>>>>>  " + parcelable.toString());
                    if (parcelable.getClass().equals(BITalinoState.class)) { //BITalino
                        Log.d(TAG,((BITalinoState) parcelable).toString());
                        action_result.setText(parcelable.toString());
                    } else if (parcelable.getClass().equals(BITalinoDescription.class)) { //BITalino
                        // isBITalino2 = ((BITalinoDescription)parcelable).isBITalino2();
                        // resultsTextView.setText("isBITalino2: " + isBITalino2 + "; FwVersion: " + String.valueOf(((BITalinoDescription)parcelable).getFwVersion()));

                    }
                }

            } else if (ACTION_EVENT_AVAILABLE.equals(action)) {
                Log.d(TAG, "PluxDevice >>>>>>>>>>>>:  ACTION_EVENT_AVAILABLE");
//                EventData event = intent.getParcelableExtra(Constants.EXTRA_EVENT);
//                String str = ";
//                if(event.eventDescription.equals(Constants.ON_BODY_EVENT)){
//                    Log.i(TAG, "[" + event.identifier + "] " + "OnBody: " + false);
//                } if(event.eventDescription.equals(Constants.BATTERY_EVENT)){
//                    Log.i(TAG, str + "Battery Level: " + event.batteryLevel);
//                }
            } else if (ACTION_DEVICE_READY.equals(action)) {
                String identifier = intent.getStringExtra(IDENTIFIER);
                // PluxDevice pluxDevice = intent.getParcelableExtra(Constants.PLUX_DEVICE);
                //Log.d(TAG, pluxDevice.toString());
                Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>  : "+identifier);
                Toast.makeText(getApplicationContext(), "PluxDevice >>>>>>>>>>>>>" + identifier + ": READY", Toast.LENGTH_LONG).show();
            } else if (Constants.ACTION_MESSAGE_SCAN.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(Constants.EXTRA_DEVICE_SCAN);
            }else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, "Discovered :"+deviceName+ " : "+deviceHardwareAddress);
                Toast.makeText(getApplicationContext(), deviceHardwareAddress, Toast.LENGTH_SHORT).show();
                action_result.setText("Found  :"+deviceHardwareAddress);
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                // run some code
                Toast.makeText(getApplicationContext(), "Discovery Started", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "ACTION_DISCOVERY_STARTED: ");
                action_result.setText("Discovery Started");
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                // run some code
                Toast.makeText(getApplicationContext(), "Discovery Stop", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "ACTION_DISCOVERY_FINISHED: ");
                action_result.setText("Discovery Finished");
            }
            else if(ACTION_STATE_CHANGED.equals(action)){
                if(mBluetoothAdapter.getState() == mBluetoothAdapter.STATE_OFF){
                    Toast.makeText(getApplicationContext(), "Bluetooth off switch on", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "ACTION_STATE_CHANGED off: ");
                    action_result.setText("Bluetooth Off");

                }
            }
        }
    };



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem action_switch_Bitalano = menu.findItem(R.id.action_switch_Bitalano);
        MenuItem action_switch_ble = menu.findItem(R.id.action_switch_ble);


        MenuItem action_on  = menu.findItem(R.id.action_on );
        MenuItem action_off = menu.findItem(R.id.action_off);
        MenuItem action_newpaired = menu.findItem(R.id.action_newpaired);
        MenuItem action_paired = menu.findItem(R.id.action_paired);

        MenuItem action_unpaired  = menu.findItem(R.id.action_unpaired);
        MenuItem action_discover = menu.findItem(R.id.action_discover);
        MenuItem action_connect = menu.findItem(R.id.action_connect);
        MenuItem action_disconnect = menu.findItem(R.id.action_disconnect);

        MenuItem bit_connect  = menu.findItem(R.id.bit_connect);
        MenuItem bit_disconnect = menu.findItem(R.id.bit_disconnect);
        MenuItem bit_start = menu.findItem(R.id.bit_start);
        MenuItem bit_version  = menu.findItem(R.id.bit_version );
        MenuItem bit_open  = menu.findItem(R.id.bit_open);
        MenuItem bit_close = menu.findItem(R.id.bit_close);



        if(isBitalano)
        {
            action_switch_Bitalano.setVisible(false);
            action_switch_ble.setVisible(true);

            action_on.setVisible(false);
            action_off.setVisible(false);
            action_newpaired.setVisible(false);
            action_unpaired.setVisible(false);
            action_discover.setVisible(false);
            action_connect.setVisible(false);
            action_disconnect.setVisible(false);
            action_paired.setVisible(false);


            bit_connect.setVisible(true);
            bit_disconnect.setVisible(true);
            bit_start.setVisible(true);
            bit_version.setVisible(true);
            bit_open.setVisible(true);
            bit_open.setVisible(true);
            bit_close.setVisible(true);
        }
        else
        {
            action_switch_Bitalano.setVisible(true);
            action_switch_ble.setVisible(false);

            action_on.setVisible(true);
            action_off.setVisible(true);
            action_newpaired.setVisible(true);
            action_unpaired.setVisible(true);
            action_discover.setVisible(true);
            action_connect.setVisible(true);
            action_disconnect.setVisible(true);
            action_paired.setVisible(true);


            bit_connect.setVisible(false);
            bit_disconnect.setVisible(false);
            bit_start.setVisible(false);
            bit_version.setVisible(false);
            bit_open.setVisible(false);
            bit_open.setVisible(false);
            bit_close.setVisible(false);
        }
        return true;



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_switch_Bitalano) {
            Toast.makeText(getApplicationContext(), "Show Bitalano Menu :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Show menu Bitano - ");
            isBitalano = true;


            return true;
        }
        if (id == R.id.action_switch_ble) {
            Toast.makeText(getApplicationContext(), "Show Bluetooth Menu :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Switch Menu - Bluetooth");
            isBitalano = false;


            return true;
        }
        if (id == R.id.action_discover) {
            Toast.makeText(getApplicationContext(), "Discover :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Discover - ");
            //TODO Add discovery method

            return true;
        }
        if (id == R.id.action_off) {
            Toast.makeText(getApplicationContext(), "BLE Turn OFF :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "BLE Off - ");
            turnOffBT();
            return true;
        }
        if (id == R.id.action_on) {
            Toast.makeText(getApplicationContext(), "BLE Turn ON :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "BLE ON - ");
            turnOnBT();

            return true;
        }
        if (id == R.id.action_paired) {
            Toast.makeText(getApplicationContext(), "Paired :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Paired - ");
            findPaired();

            return true;
        }
        if (id == R.id.action_newpaired) {
            Toast.makeText(getApplicationContext(), "Paired :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Pair again - ");
            pairDevice(dev);

            return true;
        }
        if (id == R.id.action_unpaired) {
            Toast.makeText(getApplicationContext(), "Unpair :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "un Pair - ");
            unpairDevice(dev);

            return true;
        }
        if (id == R.id.action_disconnect) {
            Toast.makeText(getApplicationContext(), "Disconnect :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Disconnect- ");
            // TODO DISCONNECT TO DEVICE


            return true;
        }
        if (id == R.id.action_connect) {
            Toast.makeText(getApplicationContext(), "Connect :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Connect - ");
            getMenu();

            // TODO CONNECT TO DEVICE

            return true;
        }
        if (id == R.id.bit_connect) {
            Toast.makeText(getApplicationContext(), "Bitalo connect :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Bitalo connect - ");
            boolean check = radioConn.isChecked();
            if(!check) {
                connectBitalino(remoteMAC);
                Log.d(TAG, "Bitalo not connected - ");
            }else{
                Log.d(TAG, "Bitalo is connect - ");

            }
            return true;
        }
        if (id == R.id.bit_disconnect) {
            Toast.makeText(getApplicationContext(), "Bitalo Disconnect :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Bitalo Disconnect - ");
            disconnectBitalino();


            return true;
        }
        if (id == R.id.bit_start) {
            Toast.makeText(getApplicationContext(), "Bitalo - Start:", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Bitalo Start - ");
            //startBitalino();
            String fr = getVersionFrame();

            Log.d("Bitalano  >>",fr);
            startBitalino();



            return true;
        }
        if (id == R.id.bit_open) {
            Toast.makeText(getApplicationContext(), "Bitalo open - :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Bitalo open - ");
            new TestAsyncTask(this).execute();


            return true;
        }
        if (id == R.id.bit_close) {
            Toast.makeText(getApplicationContext(), "Bitalo close -", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Bitalo close  - ");
            //stopBitalino();



            return true;
        }
        if (id == R.id.bit_version) {
            Toast.makeText(getApplicationContext(), "Bitalo Version -:", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Bitalo Version - ");
            boolean a = getVersion();
            if(a) {
                Log.d("Get Version >>>>>>"," True");
            }else{
                Log.d("Get Version >>>>>>"," Flase");
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView(){
        action_result = (TextView) findViewById(R.id.action_result);
        action_description = (TextView) findViewById(R.id.action_description);
        radioConn = (RadioButton) findViewById(R.id.radioConn);
        radioConn.setChecked(false);

        next = (Button) findViewById(R.id.action_next);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),GraphActivity.class);
                startActivity(intent);
            }
        });

        //myB = new MyBluetooth(mReceiver,mBluetoothAdapter);
//        dev = mBluetoothAdapter.getRemoteDevice(remoteMAC);
//        Communication communication = Communication.getById(dev.getType());
//        Log.d(TAG, "Communication:" + communication.name());
//        bthDeviceScan = new BTHDeviceScan(this);
//        Log.d(TAG,bthDeviceScan.toString());
//
//        bitalino = new BITalinoCommunicationFactory().getCommunication(communication,this);
//        handler = new Handler(getMainLooper()){
//            @Override
//            public void handleMessage(Message msg) {
//                Bundle bundle = msg.getData();
//                BITalinoFrame frame = bundle.getParcelable(FRAME);
//                Log.d(TAG, "Frame msg");
//
//                Log.d(TAG, frame.toString());
//
//                if(frame != null){ //BITalino
//                    action_result.setText(frame.toString());
//                    Log.d("Handler >>>","IIOOOOOIIIIOOOOO");
//                }
//            }
//        };

    }



    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //closeBLE();

        //cleanup();
    }

    @Override
    protected void onStart() {
        Log.d("Lifecycle >>>>>>","OnStart");
        super.onStart();

        //registerReceiver(pluxReceiver, makeUpdatePluxFilter());
        //setup();

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
        //cleanup();
        //turnOffBT();


    }

    @Override
    protected void onStop() {

        Log.d("Lifecycle >>>>>>","OnStop");
        closeBLE();
        super.onStop();


    }
    public  void closeBLE(){
        try {
            unregisterReceiver(pluxReceiver);
            bolBroacastRegistred = false;
        }catch(IllegalArgumentException e){
            e.printStackTrace();
            Log.d(TAG,"unregister Reciever Error");
        }
        try {
            bitalino.disconnect();
        } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
            e.printStackTrace();
            Log.d(TAG,"bitalino disconnect error");
        }catch (NullPointerException e){
            Log.d("Bitano Disconnect","Null Point Exception");
        }

    }

    private void cleanup(){
        try {
            unregisterReceiver(mReceiver);
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }
        if(bthDeviceScan != null){
            bthDeviceScan.closeScanReceiver();
        }
        try {
            bitalino.disconnect();
        } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
            e.printStackTrace();
            Log.d(TAG,"bitalino disconnect error");
        }



    }
    private   void setup(){
        //activeChannel = new int[4];
        //activeChannel[0] = 1;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            Log.d(TAG, "Bluetooth not Supported: ");

        }else{
            Log.d("setUp >>>>","Register reciever");
            dev = mBluetoothAdapter.getRemoteDevice(remoteMAC);
            Communication communication = Communication.getById(dev.getType());
            bitalino = new BITalinoCommunicationFactory().getCommunication(communication,this);
            registerReceiver(pluxReceiver, makeUpdatePluxFilter());

            Log.d(TAG, "Communication:" + communication.name());
//            bthDeviceScan = new BTHDeviceScan(this);
//            Log.d(TAG,bthDeviceScan.toString());

        }



        handler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                BITalinoFrame frame = bundle.getParcelable(FRAME);
                Log.d(TAG, "Frame msg");

                Log.d(TAG, frame.toString());

                if(frame != null){ //BITalino
                    action_result.setText(frame.toString());
                    Log.d("Handler >>>","IIOOOOOIIIIOOOOO");
                }
            }
        };




        mHandler= new Handler(){
            @Override
            public void handleMessage(Message msg) {

                Log.d(TAG,  "In handler");
                super.handleMessage(msg);
                switch(msg.what){
                    case SUCCESS_CONNECT:
                        // DO something
                        //ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
                        //String s = "successfully connected";
                        //connectedThread.write(s.getBytes());
                        Log.d(TAG, "SUCCESS_CONNECT in handler ");
                        break;
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[])msg.obj;
                        String string = new String(readBuf);
                        Log.d(TAG, "MESSAGE_READ in handler ");
                        //resultsTextView.append(string +"\n");
                        break;
                }
            }
        };
        ///////////////////////////////

//

        //////////////////////////////


//        Log.d(TAG,  "registering receivers");
//        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mReceiver, filter);
//        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//        registerReceiver(mReceiver, filter);
//        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        registerReceiver(mReceiver, filter);
//        filter = new IntentFilter(ACTION_STATE_CHANGED);
//        registerReceiver(mReceiver, filter);
//
//        filter = new IntentFilter(Constants.ACTION_STATE_CHANGED);
//        registerReceiver(mReceiver, filter);
//        filter = new IntentFilter(ACTION_DATA_AVAILABLE);
//        registerReceiver(mReceiver, filter);
//        filter = new IntentFilter(ACTION_COMMAND_REPLY);
//        registerReceiver(mReceiver, filter);
//        filter = new IntentFilter(ACTION_EVENT_AVAILABLE);
//        registerReceiver(mReceiver, filter);
//        filter = new IntentFilter(ACTION_DEVICE_READY);
//        registerReceiver(mReceiver, filter);
//        filter = new IntentFilter(Constants.ACTION_MESSAGE_SCAN);
//        registerReceiver(mReceiver, filter);

        //registerReceiver(pluxReceiver, makeUpdatePluxFilter());
       // registerReceiver(mReceiver, makeUpdateIntentFilter());



    }

    private IntentFilter makeUpdateIntentFilter() {
        Log.d(TAG,  "registering receivers bluetooth");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_STATE_CHANGED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        intentFilter.addAction(ACTION_EVENT_AVAILABLE);
        intentFilter.addAction(ACTION_DEVICE_READY);
        intentFilter.addAction(ACTION_COMMAND_REPLY);
        return intentFilter;
    }

    private IntentFilter makeUpdatePluxFilter(){

        final IntentFilter basicFilter = new IntentFilter();
        Log.i(TAG,"creating filters bitalano");

        try{

            basicFilter.addAction(Constants.ACTION_STATE_CHANGED);
            basicFilter.addAction(Constants.ACTION_CONNECTED);
            basicFilter.addAction(Constants.ACTION_DISCONNECTED);
            basicFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
            basicFilter.addAction(Constants.ACTION_MESSAGE_SCAN);
            basicFilter.addAction(Constants.ACTION_EVENT_AVAILABLE);
            basicFilter.addAction(Constants.ACTION_COMMAND_REPLY);
            basicFilter.addAction(Constants.ACTION_LOG_AVAILABLE);


        }
        catch (NullPointerException e)
        {
            e.getMessage();
            Log.d("Reciever Broadcast","Null Poin exception");
        }

//        Log.i(TAG,"creating filters");
//        bolBroacastRegistred = true;
//        final IntentFilter basicFilter = new IntentFilter();
//        basicFilter.addAction(Constants.ACTION_STATE_CHANGED);
//        basicFilter.addAction(Constants.ACTION_CONNECTED);
//        basicFilter.addAction(Constants.ACTION_DISCONNECTED);
//        basicFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
//        basicFilter.addAction(Constants.ACTION_MESSAGE_SCAN);
//        basicFilter.addAction(Constants.ACTION_EVENT_AVAILABLE);
//        basicFilter.addAction(Constants.ACTION_COMMAND_REPLY);
//        basicFilter.addAction(Constants.ACTION_LOG_AVAILABLE);


        /*
            ACTION_STATE_CHANGED = "info.plux.pluxapi.ACTION_STATE_CHANGED";
            ACTION_CONNECTED = "info.plux.pluxapi.ACTION_CONNECTED";
            ACTION_DISCONNECTED = "info.plux.pluxapi.ACTION_DISCONNECTED";
            ACTION_GATT_SERVICES_DISCOVERED = "info.plux.pluxapi.ACTION_GATT_SERVICES_DISCOVERED";
            ACTION_DATA_AVAILABLE = "info.plux.pluxapi.ACTION_DATA_AVAILABLE";
            ACTION_DEVICE_READY = "info.plux.pluxapi.ACTION_DEVICE";
            ACTION_MESSAGE_SCAN = "info.plux.pluxapi.ACTION_MESSAGE_SCAN";
            ACTION_EVENT_AVAILABLE = "info.plux.pluxapi.ACTION_EVENT_AVAILABLE";
            ACTION_COMMAND_REPLY = "info.plux.pluxapi.ACTION_COMMAND_REPLY";
            ACTION_LOG_AVAILABLE = "info.plux.pluxapi.ACTION_LOG_AVAILABLE";
      */



        return basicFilter;
    }


    public void turnOnBT() {
        Log.d(TAG, "Turning On Bluetooth ..: ");
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "Bluetooth not Enabled: ");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent,REQUEST_ENABLE_BT);
        }else{
            // Toast.makeText(getApplicationContext(), "Bluetooth is Enabled...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Bluetooth Enabled: ");
        }
    }

    // Turn Off Bluetooth
    public void turnOffBT(){
        Toast.makeText(getApplicationContext(), "Turning off Bluetooth", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Turning Off Bluetooth: ");
        if (mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Bluetooth turning off...", Toast.LENGTH_SHORT).show();
            mBluetoothAdapter.disable();
        }else{
            Toast.makeText(getApplicationContext(), "Bluetooth is off...", Toast.LENGTH_SHORT).show();
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
        action_result.setText("");
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


    public void startBitalino(){
        int [] a =new int[]{0,1,2,3,4,5};


        try {
            boolean start = bitalino.start(a,10);
        } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
            e.printStackTrace();
            Log.d(TAG,"Start Bitalano error ...");
        }

    }
    public  boolean connectBitalino(String add){
        boolean b = false;

        try {

            bitalino.connect(add);
            b = true;
        } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
            e.printStackTrace();
            Log.d(TAG,"Bitalano connection error ^^^^^^^^^^^^");
        }
        return b;
    }
    public void disconnectBitalino(){

        try {
            bitalino.disconnect();
        } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
            e.printStackTrace();
            Log.d(TAG,"disconnect Bitalano error ...");
        }
    }
    public void stopBitalino(){

        try {
            bitalino.stop();
        } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
            e.printStackTrace();
            Log.d(TAG,"Stop Bitalano error ...");
        }
    }
    public void trigger(){
        int[] digitalChannels;
        digitalChannels = new int[4];
        digitalChannels[0] = 0;//(digital1RadioButton.isChecked()) ? 1 : 0;
        digitalChannels[1] = 0;//(digital2RadioButton.isChecked()) ? 1 : 0;

       try {

            bitalino.trigger(digitalChannels);
        } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
           e.printStackTrace();
           Log.d(TAG," state Bitalano error ..."+e);
       }
    }
    public boolean state(){
        boolean b = false;
        try {
            b = bitalino.state();
        }  catch (info.plux.pluxapi.bitalino.BITalinoException e) {
            e.printStackTrace();
        }
        return b;
    }

    public boolean getVersion(){
        boolean b = false;
        try{
            bitalino.getVersion();
            b = true;
        } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
            e.printStackTrace();
        }
        return b;
    }
    public String getString(){
        return bitalino.toString();

    }
    public String getVersionFrame(){
        //BITalinoFrame(String identifier, int seq, int[] analog, int[] digital)
        int[] analog = {0,1,2,3};
        BITalinoFrame frames = new BITalinoFrame(remoteMAC,1,analog,null);

        String ans = frames.toString();

        return ans;

    }

    public boolean battery_threshold(int in){
        boolean b = false;
        try {
            b = bitalino.battery(in);
            //bitalino.battery(batteryThresholdSeekBar.getProgress());
        } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
            e.printStackTrace();
        }
        return b;
    }
    public boolean pwm(int in){
        boolean b = false;
        try {
            b = bitalino.pwm(in);
            //bitalino.battery(batteryThresholdSeekBar.getProgress());
        } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
            e.printStackTrace();
        }
        return b;
    }
    private void scanDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bthDeviceScan.stopScan();

                }
            }, SCAN_PERIOD);

            mScanning = true;
            bthDeviceScan.doDiscovery();
        } else {
            mScanning = false;
            bthDeviceScan.stopScan();
        }

    }

//    @Override
//    public void onBITalinoDataAvailable(BITalinoFrame bitalinoFrame) {
//        Message message = handler.obtainMessage();
//        Bundle bundle = new Bundle();
//        bundle.putParcelable(FRAME, bitalinoFrame);
//        message.setData(bundle);
//        handler.sendMessage(message);
//    }


    public void pause(int time){
        try {
            Thread.sleep(time);
        } catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"pause error ...");
        }
    }


    private class TestAsyncTask extends AsyncTask<Void,String,Void>{

        private BluetoothDevice dev = null;
        //private BluetoothSocket sock = null;
        private InputStream is = null;
        private OutputStream os = null;
        private BITalinoDevice bit;
        private Context context;

        public TestAsyncTask(Context c){
            this.context = c;
        }


        @Override
        protected Void doInBackground(Void... params) {
            try{
                final BluetoothAdapter btA = BluetoothAdapter.getDefaultAdapter();
                dev = btA.getRemoteDevice(remoteMAC);
                //dev = btA.getRemoteDevice("test");
                Log.d(TAG,"Stopping Discovery");
                btA.cancelDiscovery();

                sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
                sock.connect();


                bit = new BITalinoDevice(1000, new int[]{0,1,2,3,4,});
                Log.d(TAG,"connecting to :");

                bit.open(sock.getInputStream(),sock.getOutputStream());
                Log.d(TAG,"Connected  :");
                //Log.d(TAG, "V :"+bit.version());
                bit.start();

                int counter = 0;

                while (counter <100){
                    final int numSamples = 1000;
                    Log.d(TAG, "Reading   :"+counter);

                    com.bitalino.comm.BITalinoFrame[] fram = bit.read(numSamples);
                    for(com.bitalino.comm.BITalinoFrame frame: fram){

                        Log.d(TAG,frame.toString());

                    }


                    counter ++;

                }
                Log.d(TAG, "Reading Finished  :");




            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG,"IO Error "+e);
            } catch (BITalinoException e) {
                e.printStackTrace();
                Log.d(TAG,"IO Error "+e);
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            try{
                bit.stop();
            } catch (BITalinoException e) {
                e.printStackTrace();
                Log.d(TAG,"Stop BIT Error");
            }
        }
    }



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

    private  void stateChanged(Intent intent){
        String identifier = intent.getStringExtra(IDENTIFIER);
        Constants.States state = Constants.States.getStates(intent.getIntExtra(EXTRA_STATE_CHANGED,0));

        Log.i(TAG, identifier + " -> " + state.name());

        action_result.setText(state.name());

        switch (state){
            case NO_CONNECTION:

                break;
            case LISTEN:

                break;
            case CONNECTING:

                break;
            case CONNECTED:

                break;
            case ACQUISITION_TRYING:

                break;
            case ACQUISITION_OK:

                break;
            case ACQUISITION_STOPPING:

                break;
            case DISCONNECTED:

                break;
            case ENDED:

                break;

        }
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


    public void onBITalinoDataAvailable(BITalinoFrame bitalinoFrame) {
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putParcelable(FRAME, bitalinoFrame);
        message.setData(bundle);
        handler.sendMessage(message);
    }

}
