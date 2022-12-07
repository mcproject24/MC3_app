package com.example.mc_project_3;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.InetAddresses;
import android.net.Uri;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{
    private Uri imageUri;
    public boolean sender;

    Button wifiButton, discoverButton;
    RecyclerView recyclerView;
    MyRecyclerViewAdapter adapter;

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;

    IntentFilter intentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    List<String> deviceNames = new ArrayList<String>();
    WifiP2pDevice[] devices;

    Socket socket = new Socket();
    byte buf[]  = new byte[1024];
    int len;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        //initialize the buttons and Wifip2p elements
        initialize();

        //execute listeners on button click
        executeListeners();

        if(sender) {

            // WiFi Direct action
//            wifiButton.setOnClickListener(v -> submit());

        }
        else{
            //make the discover button invisible
            Toast.makeText(this, "Connected to Akash's Galaxy M31s", Toast.LENGTH_SHORT).show();
            discoverButton.setVisibility(View.GONE);

        }

    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            if (!peerList.equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                devices = new WifiP2pDevice[peerList.getDeviceList().size()];

                //populating the list of all device names
                int index = 0;
                for(WifiP2pDevice device: peerList.getDeviceList()){
                    deviceNames.add(device.deviceName);
                    devices[index] = device;
                    index+=1;
                }
                adapter = new MyRecyclerViewAdapter(getApplicationContext(), deviceNames);
                adapter.setClickListener(ServerActivity.this::onItemClick);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                SpacingItemDecorator margin = new SpacingItemDecorator(0);

                recyclerView.addItemDecoration(margin);
                recyclerView.setAdapter(adapter);


                if(peers.size() == 0){
                    Toast.makeText(ServerActivity.this, "No devices found",
                            Toast.LENGTH_SHORT).show();
//                    return;
                }

            }

            if (peers.size() == 0) {
                Log.d("Peers", "No devices found");
                return;
            }
        }
    };

    @Override
    public void onItemClick(View view, int position) {

        final WifiP2pDevice device = devices[position];
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ServerActivity.this, "Connected to "+device.deviceName, Toast.LENGTH_SHORT).show();
                //socket code
                try{
                    socket.bind(null);
                    socket.connect((new InetSocketAddress("Host device", 8888)), 500);

                    /**
                     * Create a byte stream from a JPEG file and pipe it to the output stream
                     * of the socket. This data is retrieved by the server device.
                     */
                    OutputStream outputStream = socket.getOutputStream();
                    ContentResolver cr = getApplicationContext().getContentResolver();
                    InputStream inputStream = null;

                    //send part data to the device instead of imageUri
                    inputStream = cr.openInputStream(imageUri);
                    while ((len = inputStream.read(buf)) != -1) {
                        outputStream.write(buf, 0, len);
                    }
                    outputStream.close();
                    inputStream.close();
                }catch(FileNotFoundException f){
                    Log.d("Exception_f", f.getMessage());
                }catch(IOException e){
                    Log.d("Exception_i", e.getMessage());
                }

                finally {
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                //catch logic
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(ServerActivity.this, "Not Connected", Toast.LENGTH_SHORT).show();
            }
        });

    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                Toast.makeText(ServerActivity.this, "Host", Toast.LENGTH_SHORT).show();
            }else if(wifiP2pInfo.groupFormed){
                Toast.makeText(ServerActivity.this, "Client", Toast.LENGTH_SHORT).show();
            }

        }
    };

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }





    private void initialize(){
        Bundle bundle = getIntent().getExtras();
        sender = bundle.getBoolean("isSender");
        if(sender){
            imageUri = bundle.getParcelable("image");
        }
//        call_server();

        wifiButton = findViewById(R.id.wifiButton);
        discoverButton = findViewById(R.id.discoverButton);
        recyclerView = findViewById(R.id.deviceList);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);



        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void executeListeners(){
        wifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });

        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ServerActivity.this, "Discovery started.",
                                Toast.LENGTH_SHORT).show();
                        Toast.makeText(ServerActivity.this, "Sending image data to devices", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d("DF", ""+i);
                        Toast.makeText(ServerActivity.this, "Discovery failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }



    public void call_server() {
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl("http://192.168.0.198:5000/").addConverterFactory(GsonConverterFactory.create());

        File f = new File(getRealPathFromURI(this, imageUri));

        RequestBody filePart = RequestBody.create(MediaType.parse(this.getContentResolver().getType(imageUri)), f);
        MultipartBody.Part file = MultipartBody.Part.createFormData("photo", f.getName(), filePart);

        Retrofit retrofit = builder.build();
        UserClient client = retrofit.create(UserClient.class);
        Log.d("client", "client configured");
        Call<ResponseBody> call = client.uploadPhoto(file);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(ServerActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    if(response.body()!=null){
//                        response.body().
//                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());

                    }
                }
                finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ServerActivity.this, "Data transfer failed", Toast.LENGTH_SHORT).show();
                Log.d("network", t.getMessage());
                finishAndRemoveTask();
            }
        });
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e("path error", "getRealPathFromURI Exception : " + e);
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}