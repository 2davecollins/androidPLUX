package com.apps.dave.davepluxandroid;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import info.plux.pluxapi.BTHDeviceScan;
import info.plux.pluxapi.Communication;
import info.plux.pluxapi.Constants;
import info.plux.pluxapi.bitalino.BITalinoCommunication;
import info.plux.pluxapi.bitalino.BITalinoCommunicationFactory;
import info.plux.pluxapi.bitalino.BITalinoDescription;
import info.plux.pluxapi.bitalino.BITalinoFrame;
import info.plux.pluxapi.bitalino.BITalinoState;

import static com.apps.dave.davepluxandroid.R.layout.activity_main;
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

    ArrayList<BluetoothDevice> bleDevices;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice dev;
    IntentFilter filter;
    Handler mHandler;

    private Handler handler;

    private BTHDeviceScan bthDeviceScan;
    private boolean mScanning;
    private BITalinoCommunication bitalino;


    TextView action_result;
    TextView action_description;

    private RadioButton radioConn;
    private Button next;


    MyBluetooth myB;


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
                    //cleanup();
                    //stopBitalino();

                }else if(state.name().equals("CONNECTED")){
                    radioConn.setChecked(true);
                    startBitalino();

                }else{


                }
            } else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
               // BITalinoFrame frames = intent.getParcelableExtra(Constants.EXTRA_DATA);

                Log.d(TAG, "Action Data Available >>>>>>>>>>>  " );
                if(intent.hasExtra(EXTRA_DATA)){
                    Parcelable parcelable = intent.getParcelableExtra(EXTRA_DATA);
                    if(parcelable.getClass().equals(BITalinoFrame.class)){ //BITalino
                        action_result.setText(parcelable.toString());
                        Log.d(TAG, ">>>>>>>>>>>  "+parcelable.toString() );
                    }
                }

            } else if (Constants.ACTION_COMMAND_REPLY.equals(action)) {

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

            } else if (Constants.ACTION_EVENT_AVAILABLE.equals(action)) {
                Log.d(TAG, "PluxDevice >>>>>>>>>>>>:  ACTION_EVENT_AVAILABLE");
//                EventData event = intent.getParcelableExtra(Constants.EXTRA_EVENT);
//                String str = ";
//                if(event.eventDescription.equals(Constants.ON_BODY_EVENT)){
//                    Log.i(TAG, "[" + event.identifier + "] " + "OnBody: " + false);
//                } if(event.eventDescription.equals(Constants.BATTERY_EVENT)){
//                    Log.i(TAG, str + "Battery Level: " + event.batteryLevel);
//                }
            } else if (Constants.ACTION_DEVICE_READY.equals(action)) {
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
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                // run some code
                Toast.makeText(getApplicationContext(), "Discovery Started", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "ACTION_DISCOVERY_STARTED: ");
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                // run some code
                Toast.makeText(getApplicationContext(), "Discovery Stop", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "ACTION_DISCOVERY_FINISHED: ");
            }
            else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                if(mBluetoothAdapter.getState() == mBluetoothAdapter.STATE_OFF){
                    Toast.makeText(getApplicationContext(), "Bluetooth off switch on", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "ACTION_STATE_CHANGED off: ");

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getApplicationContext(), "Start Connevtion :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Start Connection - ");
            startBitalino();

            return true;
        }
        if (id == R.id.action_discover) {
            Toast.makeText(getApplicationContext(), "Discover :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Discover - ");
            trigger();


            return true;
        }
        if (id == R.id.action_off) {
            Toast.makeText(getApplicationContext(), "BLE Turn OFF :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "BLE Off - ");
            stopBitalino();
            pause(1000);
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
            unpairDevice(dev);

            return true;
        }
        if (id == R.id.action_newpaired) {
            Toast.makeText(getApplicationContext(), "Paired :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Pair again - ");
            pairDevice(dev);

            return true;
        }
        if (id == R.id.action_unpaired) {
            Toast.makeText(getApplicationContext(), "Paired :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "un Pair - ");
            unpairDevice(dev);

            return true;
        }
        if (id == R.id.action_disconnect) {
            Toast.makeText(getApplicationContext(), "Paired :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Disconnect- ");
            stopBitalino();
            disconnectBitalino();

            return true;
        }
        if (id == R.id.action_connect) {
            Toast.makeText(getApplicationContext(), "Connect :", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Connect - ");
            connectBitalino(remoteMAC);
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
        dev = mBluetoothAdapter.getRemoteDevice(remoteMAC);
        Communication communication = Communication.getById(dev.getType());
        Log.d(TAG, "Communication:" + communication.name());

        bitalino = new BITalinoCommunicationFactory().getCommunication(communication,this);

    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cleanup();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanup();

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
        if(bitalino != null){
            bitalino.closeReceivers();

            try {
                bitalino.disconnect();
            } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
                e.printStackTrace();
                Log.d(TAG,"bitalino disconnect error");
            }
        }


    }
    private   void setup(){
        //activeChannel = new int[4];
        //activeChannel[0] = 1;
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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            Log.d(TAG, "Bluetooth not Supported: ");

        }else{


        }
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        filter = new IntentFilter(Constants.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(Constants.ACTION_DATA_AVAILABLE);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(Constants.ACTION_COMMAND_REPLY);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(Constants.ACTION_EVENT_AVAILABLE);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(Constants.ACTION_DEVICE_READY);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(Constants.ACTION_MESSAGE_SCAN);
        registerReceiver(mReceiver, filter);

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
    public  void connectBitalino(String add){
        try {
            bitalino.connect(add);
        } catch (info.plux.pluxapi.bitalino.BITalinoException e) {
            e.printStackTrace();
            Log.d(TAG,"Bitalano connection error ^^^^^^^^^^^^");
        }
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
    public void pause(int time){
        try {
            Thread.sleep(time);
        } catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"pause error ...");
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


    public void onBITalinoDataAvailable(BITalinoFrame bitalinoFrame) {
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putParcelable(FRAME, bitalinoFrame);
        message.setData(bundle);
        handler.sendMessage(message);
    }

}
