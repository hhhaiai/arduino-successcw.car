package com.example.mycar;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MyCarStreamView extends SurfaceView implements SurfaceHolder.Callback {
   
	public static final String TAG="MJPEG";
    
    SurfaceHolder holder;
	Context saved_context;
    
    private MjpegViewThread thread;
    private MyCarInputStream mIn = null;    
    public static boolean mRun = false;
    private boolean surfaceDone = false;    

    private int dispWidth;
    private int dispHeight;
	private Bitmap bmp = null;
	
	// hard-coded image size
	public static final int IMG_WIDTH=640;
	public static final int IMG_HEIGHT=480;

    public MyCarStreamView(Context context, AttributeSet attrs) { 
        super(context, attrs); 
    	holder = getHolder();
    	saved_context = context;
        holder.addCallback(this);
        thread = new MjpegViewThread(holder, context);
        setFocusable(true);
        dispWidth = getWidth();
        dispHeight = getHeight();
    }
	
    public class MjpegViewThread extends Thread {
        private SurfaceHolder mSurfaceHolder;
        
        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) { 
            mSurfaceHolder = surfaceHolder; 
        }

        public void setSurfaceSize(int width, int height) {
            synchronized(mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            }
        }

        public void run() {
            Paint p = new Paint();
            Log.d(MyCarActivity.TAG,"almost there, started run process to display stream");
            while (mRun) {

                Rect destRect=null;
                Canvas c = null;

                if(surfaceDone) {
                	try {
            			bmp = mIn.readMjpegFrame();

                        destRect = new Rect(0, 0, dispWidth, dispHeight);
                        
                        c = mSurfaceHolder.lockCanvas();
                        synchronized (mSurfaceHolder) {
                           	c.drawBitmap(bmp, null, destRect, p);
                        }

                    }catch (IOException e){
                	
                }finally { 
                    	if (c != null) mSurfaceHolder.unlockCanvasAndPost(c); 
                    }
                }
            }
        }
    }
    
    public void startPlayback(MyCarInputStream source) {
    	mIn = source;
        if(mIn != null) {
            mRun = true;
            thread.start();    		
        }
    }

    public void stopPlayback() { 
        mRun = false;
        boolean retry = true;
        while(retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {}
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
        thread.setSurfaceSize(w, h); 
    }

    public void surfaceDestroyed(SurfaceHolder holder) { 
        surfaceDone = false;
        stopPlayback();
    }
    
    public void surfaceCreated(SurfaceHolder holder) { 
    	surfaceDone = true;
	}
}
