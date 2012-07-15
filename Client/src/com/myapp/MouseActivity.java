package com.myapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MouseActivity  extends Activity implements SensorEventListener, View.OnClickListener {
	private SensorManager sensorManager;

	TextView xCoor; // declare X axis object
	TextView yCoor; // declare Y axis object
	TextView zCoor; // declare Z axis object
	
	EditText ip;

	Boolean flag;
	Button start,lkm,rkm;
	int Px,Py,PxStart=0,PyStart=0,lk=0,rk=0,dx=0,dy=0,xstart=0,ystart=0;
	OutputStream out;
	Socket s;
	@Override
	public void onCreate(Bundle savedInstanceState){  

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		xCoor=(TextView)findViewById(R.id.xcoor); // create X axis object
		yCoor=(TextView)findViewById(R.id.ycoor); // create Y axis object
		zCoor=(TextView)findViewById(R.id.zcoor); // create Z axis object
		start = (Button)findViewById(R.id.start);
		
		lkm = (Button)findViewById(R.id.lkm);
		rkm = (Button)findViewById(R.id.rkm);
		
		ip = (EditText)findViewById(R.id.ip);
		//ip.setText();
		
		
		
		lkm.setOnClickListener(this);
		rkm.setOnClickListener(this);
		start.setOnClickListener(this);
		
		
		sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
		
		sensorManager.registerListener(this,
			sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
			SensorManager.SENSOR_DELAY_FASTEST);

		/*	More sensor speeds (taken from api docs)
		    SENSOR_DELAY_FASTEST get sensor data as fast as possible
		    SENSOR_DELAY_GAME	rate suitable for games
		 	SENSOR_DELAY_NORMAL	rate (default) suitable for screen orientation changes
		*/
		 }

		 public void onAccuracyChanged(Sensor sensor,int accuracy){

		 }

		 public void onSensorChanged(SensorEvent event){

		// check sensor type
		 	if(event.sensor.getType()==Sensor.TYPE_ORIENTATION){

			// assign directions
		 		float x=event.values[0];
		 		float y=event.values[1];
		 		float z=event.values[2];
		 		Px=(int)x-PxStart;
		 		
		 		Py=(int)y-PyStart;
			//if (Py<0) Py=Math.abs(Py); else Py=360-Py;
			//Py=360-Py;
		 		Py=Py*(-1);
		 		
		 		xCoor.setText("X: "+Px);
		 		yCoor.setText("Y: "+Py);
		 		zCoor.setText("Z: "+z);
			//lrk.setText(lkm.isPressed() ? "lrk pressed" : "lrk released");
			//Arrays.toString(buf) + " (" + Px + "," + Py + ")"
		 		if (out != null)
		 		{
		 			byte[] buf=ByteBuffer.allocate(14).
		 			putInt(Px).
		 			putInt(Py).
		 			putInt(dy).
		 			put((byte) (lkm.isPressed() ? 1 : 0)).
		 			put((byte) (rkm.isPressed() ? 1 : 0)).
		 			
		 			array();
		 			
		 			try {
		 				out.write(buf, 0, 14);
		 			} catch (IOException e) {
				// TODO Auto-generated catch block
		 				e.printStackTrace();
		 			}
		 			
		 		}
		 	}
		 }

		 @Override
		 public void onClick(View view) {
		// TODO Auto-generated method stub
		 	
		 	
		 	try
		 	{
		 		switch (view.getId()) {
		 			case R.id.start:
		 			if (out==null)
		 			{
		 				start.setText("Stop");
		 				PxStart=Px;
		 				PyStart=-Py;
		 				s = new Socket(ip.getText().toString(),5382);
		 				out = s.getOutputStream();
		 				Toast.makeText(this,"Service is started.", Toast.LENGTH_LONG).show();
		 			}
		 			else 
		 			{
		 				start.setText("Start");
		 				PxStart=0;
		 				PyStart=0;
		 				Toast.makeText(this,"Service is stopped.", Toast.LENGTH_LONG).show();
		 				out = null;
		 				s.shutdownOutput();
		 				s.close();
		 			}
		 			break;
		 			
		 			
		 			
		 			case R.id.lkm:
		 			lk=1;
		 			break;
		 			
		 			case R.id.rkm:
		 			rk=1;
		 			break;
		 			
		 			default: 
		 			{
		 				lk=0;
		 				rk=0;
		 			}
		 		}
		 	}
		 	catch (Exception e) {}
		 	
		 }
		 
		 public boolean onTouchEvent(MotionEvent event) {
		 	int x = (int)event.getX();
		 	int y = (int)event.getY();
		 	
		 	switch (event.getAction()) {
		 		case MotionEvent.ACTION_DOWN:
		 		
		 		dx=0;
		 		dy=0;
		 		xstart=x;
		 		ystart=y;
	        	//Toast.makeText(this,"Down", Toast.LENGTH_LONG).show();
	        	//lrk.setText("Down"+x+" "+" "+y);
		 		break;
		 		case MotionEvent.ACTION_MOVE:
	        	//Toast.makeText(this,"Move", Toast.LENGTH_LONG).show();
		 		dx=(int)(xstart-x);
		 		dy=(int)(1000*(ystart-y)/800);
		 		
		 		break;
		 		case MotionEvent.ACTION_UP:
		 		dx=0;xstart=x;
		 		dy=0;ystart=y;
	        	//Toast.makeText(this,"Up", Toast.LENGTH_LONG).show();
	        	//lrk.setText("Up"+x+" "+" "+y);
		 		break;
		 	}
		 	
		 	return true;
		 }

		 
		}
