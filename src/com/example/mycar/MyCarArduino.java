package com.example.mycar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MyCarArduino extends Activity implements SensorEventListener {
	//private boolean inLOOP = false;
	private  AsyncTask<String, Void, MyCarInputStream> Stream_;
	private Socket client;
	
	private ToggleButton OnOffButton;
	private SensorManager sensormanager;
	private	Sensor accelerometer;
	private Sensor magneticsensor;
	float[] accelovalues = new float[3];
	float[] magnetvalues = new float[3];
	float[] rotmatrix = new float[9];
	float[] tiltvalues = new float[3];
	float startX;
	float startY;
	float currentX;
	float currentY;
	float deltaX;
	float deltaY;
	float volts;
	float distance;
	TextView displaydistance;
	TextView displaydistance2;
	TextView displaydistance3;

	private MyCarStreamView mjpegview = null;
	
	private float x,y,z;
	private int pre_status = 0; //stop
	private int STATUS_STOP = 0;
	private int STATUS_GO = 1;
	private int STATUS_BACK = 2;
	private int STATUS_LEFT = 3;
	private int STATUS_RIGHT = 4;
	private int STATUS_GO_LEFT = 5;
	private int STATUS_GO_RIGHT = 6;
	private int STATUS_BACK_LEFT = 7;
	private int STATUS_BACK_RIGHT = 8;

	private final static int REQUEST_CONNECT_DEVICE = 2;    //宏定义查询设备句柄
    private String smsg = "";    //显示用数据缓存
    private String fmsg = "";    //保存用数据缓存
	private InputStream is;    //输入流，用来接收蓝牙数据
	BluetoothDevice _device = null;     //蓝牙设备
	BluetoothSocket _socket = null;      //蓝牙通信socket
	private BluetoothAdapter _bluetooth;
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
    boolean bRun = true;
    boolean bThread = false;
    boolean bConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);    
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.arduino_activity);
        
        //inLOOP = false;
        mjpegview = (MyCarStreamView) findViewById(R.id.mjpegview);
        
        OnOffButton = (ToggleButton) findViewById(R.id.onoff);
        
        displaydistance = (TextView) findViewById(R.id.textView1);
        displaydistance.setText("Distance to object: " + distance + " in.");
        displaydistance2 = (TextView) findViewById(R.id.textView2);
        displaydistance2.setText("Distance to object: " + distance + " in.");
        displaydistance3 = (TextView) findViewById(R.id.textView3);
        displaydistance3.setText("return value");
        
        _bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (!_bluetooth.isEnabled())
        {
          // prompt the user to turn BlueTooth on
          Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
          startActivityForResult(enableBtIntent, 1);
        }
        if (_bluetooth.isEnabled()) {
        	BTConnect();
        }
        
        //setup sensors
        sensormanager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //accelerometer = sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //magneticsensor = sensormanager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        new Looper().start();
        
    }
  //连接按键响应函数
    public void BTConnect(){ 
    	if(_bluetooth.isEnabled()==false){  //如果蓝牙服务不可用则提示
    		Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
    		return;
    	}
    		
        //如未连接设备则打开DeviceListActivity进行设备搜索
    	if(_socket==null){
    		Intent serverIntent = new Intent(this, DeviceListActivity.class); //跳转程序设置
    		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
    	}
    	else{
    		 //关闭连接socket
    	    try{
    	    	
    	    	is.close();
    	    	_socket.close();
    	    	_socket = null;
    	    	bRun = false;
    	    }catch(IOException e){}   
    	}
    	return;
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch(requestCode){
    	case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
    		// 响应返回结果
            if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
                // MAC地址，由DeviceListActivity设置返回
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // 得到蓝牙设备句柄      
                _device = _bluetooth.getRemoteDevice(address);
 
                // 用服务号得到socket
                try{
                	_socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                }catch(IOException e){
                	Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                }
                //连接socket

                try{
                	_socket.connect();
                	Toast.makeText(this, "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();

                }catch(IOException e){
                	try{
                		Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                		_socket.close();
                		_socket = null;
                	}catch(IOException ee){
                		Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                	}
                	
                	return;
                }
                
                bConnected = true;
                //打开接收线程
                try{
            		is = _socket.getInputStream();   //得到蓝牙数据输入流
            		}catch(IOException e){
            			Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
            			return;
            		}
            		if(bThread==false){
            			ReadThread.start();
            			bThread=true;
            		}else{
            			bRun = true;
            		}
            }
    		break;
    	default:break;
    	}
    	//new Looper().start();
    }
 Thread ReadThread=new Thread(){
    	
    	public void run(){
    		int num = 0;
    		byte[] buffer = new byte[1024];
    		byte[] buffer_new = new byte[1024];
    		int i = 0;
    		int n = 0;
    		bRun = true;
    		//接收线程
    		while(true){
    			try{
    				while(is.available()==0){
    					while(bRun == false){}
    				}
    				while(true){
    					num = is.read(buffer);         //读入数据
    					n=0;
    					
    					String s0 = new String(buffer,0,num);
    					fmsg+=s0;    //保存收到数据
    					for(i=0;i<num;i++){
    						if((buffer[i] == 0x0d)&&(buffer[i+1]==0x0a)){
    							buffer_new[n] = 0x0a;
    							i++;
    						}else{
    							buffer_new[n] = buffer[i];
    						}
    						n++;
    					}
    					String s = new String(buffer_new,0,n);
    					System.out.println("bruce"+s);
    					smsg+=s;   //写入接收缓存
    					if(is.available()==0)break;  //短时间没有数据才跳出进行显示
    				}
    				//发送显示消息，进行显示刷新
    						
    					handler.sendMessage(handler.obtainMessage());       	    		
    	    		}catch(IOException e){
    	    		}
    		}
    	}
    };
    
    //消息处理队列
    Handler handler= new Handler(){
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		//System.out.println("bruce"+smsg);
    		//displaydistance.setText(smsg);
    		//Toast.makeText(MyCarArduino.this, smsg, Toast.LENGTH_SHORT).show();
    	}
    };
    
    public void BTSend(int status){
    	int i=0;
    	try{
    		if(bConnected == true) {
	    		OutputStream os = _socket.getOutputStream();   //蓝牙连接输出流
	    		byte[] bos = new byte[3];
	    		switch(status){
	    		case 0: //STATUS_STOP
		    		bos[0] = '0';
		    		bos[1] = 0x0d;
		    		bos[2] = 0x0a;
	    			break;
	    		case 1: //STATUS_GO
		    		bos[0] = '1';
		    		bos[1] = 0x0d;
		    		bos[2] = 0x0a;
	    			break;
	    		case 2: //STATUS_BACK
		    		bos[0] = '2';
		    		bos[1] = 0x0d;
		    		bos[2] = 0x0a;
	    			break;
	    		case 3: //STATUS_LEFT
		    		bos[0] = '3';
		    		bos[1] = 0x0d;
		    		bos[2] = 0x0a;
	    			break;
	    		case 4: //STATUS_RIGHT
		    		bos[0] = '4';
		    		bos[1] = 0x0d;
		    		bos[2] = 0x0a;
	    			break;	
	    		case 5: //STATUS_GO_LEFT
		    		bos[0] = '5';
		    		bos[1] = 0x0d;
		    		bos[2] = 0x0a;
	    			break;
	    		case 6: //STATUS_GO_RIGHT
		    		bos[0] = '6';
		    		bos[1] = 0x0d;
		    		bos[2] = 0x0a;
	    			break;
	    		case 7: //STATUS_BACK_LEFT
		    		bos[0] = '7';
		    		bos[1] = 0x0d;
		    		bos[2] = 0x0a;
	    			break;	
	    		case 8: //STATUS_BACK_RIGHT
		    		bos[0] = '8';
		    		bos[1] = 0x0d;
		    		bos[2] = 0x0a;
	    			break;	    			
	    		}

	    		
	    		os.write(bos);
    		}
    	}catch(IOException e){  		
    	}  	
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
//    	sensormanager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
//    	sensormanager.registerListener(this, magneticsensor, SensorManager.SENSOR_DELAY_GAME);
    	List<Sensor> sensors = sensormanager.getSensorList(Sensor.TYPE_ACCELEROMETER);
    	if(sensors.size()>0){
    		sensormanager.registerListener(this, sensors.get(0),SensorManager.SENSOR_DELAY_FASTEST);
    	}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if (Stream_ != null) {
    		Stream_.cancel(true);
    	}
		if (client != null){
			try {
				client.shutdownInput();
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        if(mjpegview!=null){
        	mjpegview.stopPlayback();
        }
    	sensormanager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_mycar, menu);
        return true;
    }
    
    @Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
//		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//			System.arraycopy(event.values, 0, accelovalues, 0, 3);
//
//		}
//		else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//			System.arraycopy(event.values, 0, magnetvalues, 0, 3);
//		}
		x = event.values[SensorManager.DATA_X];
		y = event.values[SensorManager.DATA_Y];
		z = event.values[SensorManager.DATA_Z];
		
//		System.out.println("x = " + x);
//		System.out.println("y = " + y);
//		System.out.println("z = " + z);
		
		displaydistance2.setText("x = " + x + "y = " + y + "z = " +z);
		if( x <= 3.0 && x >= 0 && y <= 2.0 && y >= -2.0) {
			if(pre_status != STATUS_GO) {
				System.out.println("go");
				BTSend(STATUS_GO);
				pre_status = STATUS_GO;
				System.out.println("x = " + x);
				System.out.println("y = " + y);
				System.out.println("z = " + z);
				displaydistance.setText("x = " + x + "y = " + y + "z = " +z + "GO");
			}
		} else if (x >= 7.0 && y <= 2.0 && y >= -2.0){
			if(pre_status != STATUS_BACK) {
				System.out.println("back");
				BTSend(STATUS_BACK);
				pre_status = STATUS_BACK;
				System.out.println("x = " + x);
				System.out.println("y = " + y);
				System.out.println("z = " + z);
				displaydistance.setText("x = " + x + "y = " + y + "z = " +z + "BACK");
			}
		} else if (y < -2.0 && x > 3.0 && x < 7.0) {
			if(pre_status != STATUS_LEFT) {
				System.out.println("left");
				BTSend(STATUS_LEFT);
				pre_status = STATUS_LEFT;
				System.out.println("x = " + x);
				System.out.println("y = " + y);
				System.out.println("z = " + z);
				displaydistance.setText("x = " + x + "y = " + y + "z = " +z + "LEFT");
			}
		} else if (y > 2.0 && x > 3.0 && x < 7.0) {
			if(pre_status != STATUS_RIGHT) {
				System.out.println("right");
				BTSend(STATUS_RIGHT);
				pre_status = STATUS_RIGHT;
				System.out.println("x = " + x);
				System.out.println("y = " + y);
				System.out.println("z = " + z);
				displaydistance.setText("x = " + x + "y = " + y + "z = " +z + "RIGHT");
			}
		} else if (x <= 3.0 && y < -2.0) {
			if(pre_status != STATUS_GO_LEFT) {
				System.out.println("go left");
				BTSend(STATUS_GO_LEFT);
				pre_status = STATUS_GO_LEFT;
				System.out.println("x = " + x);
				System.out.println("y = " + y);
				System.out.println("z = " + z);
				displaydistance.setText("x = " + x + "y = " + y + "z = " +z + "GO_LEFT");
			}
		} else if (x <= 3.0 && y > 2.0) {
			if(pre_status != STATUS_GO_RIGHT) {
				System.out.println("go right");
				BTSend(STATUS_GO_RIGHT);
				pre_status = STATUS_GO_RIGHT;
				System.out.println("x = " + x);
				System.out.println("y = " + y);
				System.out.println("z = " + z);
				displaydistance.setText("x = " + x + "y = " + y + "z = " +z + "GO_RIGHT");
			}
		} else if (x >= 7.0 && y < -2.0) {
			if(pre_status != STATUS_BACK_LEFT) {
				System.out.println("back left");
				BTSend(STATUS_BACK_LEFT);
				pre_status = STATUS_BACK_LEFT;
				System.out.println("x = " + x);
				System.out.println("y = " + y);
				System.out.println("z = " + z);
				displaydistance.setText("x = " + x + "y = " + y + "z = " +z + "BACK_LEFT");
			}
		} else if (x >= 7.0 && y > 2.0) {
			if(pre_status != STATUS_BACK_RIGHT) {
				System.out.println("back right");
				BTSend(STATUS_BACK_RIGHT);
				pre_status = STATUS_BACK_RIGHT;
				System.out.println("x = " + x);
				System.out.println("y = " + y);
				System.out.println("z = " + z);
				displaydistance.setText("x = " + x + "y = " + y + "z = " +z + "BACK_RIGHT");
			}
		} else if ((y < 1.0 && y > -1.0) || (x < 6.0 && x > 4.0)){
			if(pre_status != STATUS_STOP) {
				System.out.println("stop");
				BTSend(STATUS_STOP);
				pre_status = STATUS_STOP;
				System.out.println("x = " + x);
				System.out.println("y = " + y);
				System.out.println("z = " + z);
				displaydistance.setText("x = " + x + "y = " + y + "z = " +z + "STOP");
			}
		} else {
			if(pre_status != STATUS_STOP) {
				System.out.println("stop");
				BTSend(STATUS_STOP);
				pre_status = STATUS_STOP;
				System.out.println("x = " + x);
				System.out.println("y = " + y);
				System.out.println("z = " + z);
				displaydistance.setText("x = " + x + "y = " + y + "z = " +z + "STOP");
			}
		}

	}

	@Override
	public boolean onTouchEvent (MotionEvent event)	{
		int action = event.getActionMasked();
		switch (action) {

			case MotionEvent.ACTION_DOWN:
				startX = event.getRawX();
				startY = event.getRawY();
				break;

			case MotionEvent.ACTION_MOVE:
				currentX = event.getX();
				currentY = event.getY();
				//Log.d("touchevent", "rawX= " + startX + " currentX= " + currentX);
				//Log.d("touchevent", "rayY= " + startY + " currentY= " + currentY);
				//compute how much your finger has moved and normalize it between 0 and 1
				deltaX = (currentX - startX);
				deltaY = (currentY - startY);
				//Log.d("touchevent", "deltaX= " + deltaX + " deltaY= " + deltaY);
				break;

			case MotionEvent.ACTION_UP:
				//make motors stop and center servos
				deltaX = 0.0f;
				//Log.d("touchevent", "stop");
				//Log.d("touchevent", "deltaX= " + deltaX + " deltaY= " + deltaY);
				break;


		}
		return true;
	}
    
    class Looper extends Thread {	
    	@Override
    	public void run() {
    		Log.d("MyCar", "enter loop!");

    		//if (OnOffButton.isChecked()) {
    			 
    			 //get sensor info
    			SensorManager.getRotationMatrix(rotmatrix, null, accelovalues, magnetvalues);
    			SensorManager.getOrientation(rotmatrix, tiltvalues);

    			try {
    				Thread.sleep(200);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    	        if (Stream_ == null){
    	        	Stream_ = new ReadStream();
    	        }
    		
    			Log.d("MyCar", "enter loop" + Stream_.getStatus());
    		if(Stream_.getStatus() != AsyncTask.Status.RUNNING && Stream_.getStatus() != AsyncTask.Status.FINISHED ){
    			Log.d(MyCarActivity.TAG,"entered ReadStream loop");
    			Stream_.execute("fake");
    		}
		//}
    	}
	}
    	
    
    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public class ReadStream extends AsyncTask<String, Void, MyCarInputStream> {
    	
        protected MyCarInputStream doInBackground(String... url) {
        	Log.d(MyCarActivity.TAG,"ReadStream doInBackground");
        	//inLOOP = true;
            try {
            	ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(MyCarActivity.TAG, "Server: Socket opened");
                client = serverSocket.accept();                             //wait for connection from client
                serverSocket.close();
                Log.d(MyCarActivity.TAG, "Server: connection done");
                InputStream inputstream = client.getInputStream();
                return new MyCarInputStream(inputstream);  
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(MyCarInputStream result) {
        	if (result == null){
        		Log.d(MyCarActivity.TAG,"result is null");
        	}
        	if (mjpegview == null){
        		Log.d(MyCarActivity.TAG,"mjpegview is null");
        	}
        	Log.d(MyCarActivity.TAG,"starting playback!");
			mjpegview = (MyCarStreamView) findViewById(R.id.mjpegview); 
        	mjpegview.startPlayback(result);
    	    //Intent intent = new Intent(context_, MyCarArduino.class);
    	    //startActivity(intent);
        }
    }
    
}
