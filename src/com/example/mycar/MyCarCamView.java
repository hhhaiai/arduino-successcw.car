package com.example.mycar;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MyCarCamView implements SurfaceHolder.Callback{
    public static interface CameraReadyCallback { 
        public void onCameraReady(); 
    }

    private Camera camera_ = null;
    private SurfaceHolder surfaceHolder_ = null;
    private SurfaceView	  surfaceView_;
    CameraReadyCallback cameraReadyCb_ = null;
    private static int BACK_CAMERA = 0;
    //private static int FRONT_CAMERA = 1;
	public static final int IMG_WIDTH=640;
	public static final int IMG_HEIGHT=480;


    @SuppressWarnings("deprecation")
	public MyCarCamView(SurfaceView sv){
        surfaceView_ = sv;

        surfaceHolder_ = surfaceView_.getHolder();
        surfaceHolder_.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder_.addCallback(this); 
    }

    public void setCameraReadyCallback(CameraReadyCallback cb) {
        cameraReadyCb_ = cb;
    }

    public void StartPreview(){
        if ( camera_ == null)
            return;
        camera_.startPreview();
    }
    
    public void StopPreview(){
        if ( camera_ == null)
            return;
        camera_.stopPreview();
    }
    
    public void Release() {
        if ( camera_ != null) {
            camera_.stopPreview();
            camera_.release();
            camera_ = null;
        }
    }
    
    public void setupCamera(int wid, int hei, PreviewCallback cb) {
  
        Camera.Parameters p = camera_.getParameters();        
        p.setPreviewSize(IMG_WIDTH, IMG_HEIGHT);
        camera_.setParameters(p);
        
        camera_.setPreviewCallback(cb);
    }

    private void setupCamera() {
        camera_ = safeCameraOpen();
        
        if (camera_!=null) {
	        Camera.Parameters p = camera_.getParameters();        

	        p.setPreviewSize(IMG_WIDTH, IMG_HEIGHT);
	        
	        camera_.setParameters(p);
	        try {
	            camera_.setPreviewDisplay(surfaceHolder_);
	        } catch ( Exception ex) {
	            ex.printStackTrace(); 
	        }
	        camera_.startPreview();
        }
    }
    
	private Camera safeCameraOpen() {
	    Camera c = null;
	    try {
	        c = Camera.open(BACK_CAMERA); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}

    @Override
    public void surfaceChanged(SurfaceHolder sh, int format, int w, int h){
    }
    
	@Override
    public void surfaceCreated(SurfaceHolder sh){        
        setupCamera();        
        if ( cameraReadyCb_ != null)
            cameraReadyCb_.onCameraReady();
    }
    
	@Override
    public void surfaceDestroyed(SurfaceHolder sh){
        Release();
    }
}
