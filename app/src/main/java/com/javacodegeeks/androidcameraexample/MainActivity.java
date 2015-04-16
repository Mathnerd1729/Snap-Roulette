package com.javacodegeeks.androidcameraexample;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.CycleInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Camera mCamera;
	private CameraPreview mPreview;
	private PictureCallback mPicture;
    private static ImageView polaroidFrame;
    private ImageView cameraSwap;
	private Context myContext;
	private LinearLayout cameraPreview;
	private boolean cameraFront = false;
    private static RelativeLayout master;
    private static ImageView snapShot;
    private static File myPicture;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		myContext = this;
		initialize();
	}



	public void onResume() {
		super.onResume();
		if (!hasCamera(myContext)) {
			Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
			toast.show();
			finish();
		}
		if (mCamera == null) {
			//if the front facing camera does not exist
			if (findFrontFacingCamera() < 0) {
				Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
				cameraSwap.setVisibility(View.GONE);
			}			
			mCamera = Camera.open(findBackFacingCamera());
			mPicture = getPictureCallback();
			mPreview.refreshCamera(mCamera);
		}
	}

	public void initialize() {
		cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
		mPreview = new CameraPreview(myContext, mCamera);
		cameraPreview.addView(mPreview);
        master = (RelativeLayout) findViewById(R.id.masterView);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        System.out.println("Screen size: " + width + " x " + height);



            ImageView marquee = new ImageView(this);
            marquee.setImageResource(R.drawable.snaproulette);
        {
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(width / 2, width / 3);
            layout.leftMargin = width / 2 - width / 4;
            layout.topMargin = -50;
            marquee.setLayoutParams(layout);

        }

            View topBlackout = new View(this);
            int topBlackoutWidth = width;
            int topBlackoutHeight = (int)(height-width*(0.95))/2;
        {
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(topBlackoutWidth,topBlackoutHeight);
            topBlackout.setLayoutParams(layout);
            topBlackout.setAlpha(0.5f);
            topBlackout.setBackgroundColor(Color.BLACK);

        }


            View leftBlackout = new View(this);
            int leftBlackoutWidth = (int) (width*(0.025));
            int leftBlackoutHeight = (int) (width*(0.95));
        {
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(leftBlackoutWidth,leftBlackoutHeight);
            leftBlackout.setLayoutParams(layout);
            layout.topMargin = topBlackoutHeight;
            leftBlackout.setAlpha(0.5f);
            leftBlackout.setBackgroundColor(Color.BLACK);

        }

            View rightBlackout = new View(this);
        {
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(leftBlackoutWidth,leftBlackoutHeight);
            rightBlackout.setLayoutParams(layout);
            layout.topMargin = topBlackoutHeight;
            layout.leftMargin = width-leftBlackoutWidth;
            rightBlackout.setAlpha(0.5f);
            rightBlackout.setBackgroundColor(Color.BLACK);

        }

            View bottomBlackout = new View(this);

        {
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(topBlackoutWidth,topBlackoutHeight);
            bottomBlackout.setLayoutParams(layout);
            layout.topMargin = height - topBlackoutHeight;
            bottomBlackout.setAlpha(0.5f);
            bottomBlackout.setBackgroundColor(Color.BLACK);

        }

            View pictureSquare = new View(this);

        {
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams((int)(width*(0.95)),(int)(width*(0.95)));
            pictureSquare.setLayoutParams(layout);
            layout.topMargin = topBlackoutHeight;
            layout.leftMargin = leftBlackoutWidth;

            GradientDrawable gd = new GradientDrawable();
            gd.setColor(0x00000000); // Changes this drawbale to use a single color instead of a gradient
            gd.setCornerRadius(5);
            gd.setStroke(3, 0xFFFFFFFF);
            pictureSquare.setBackgroundDrawable(gd);

        }

            View wheel = new RouletteWheel(this, 0, 0, width/6, width/6);

        {
            wheel.setOnClickListener(captrureListener);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(width/6,width/6);
            layout.leftMargin = width/2 - width/12;
            layout.topMargin = height - width/6-width/12;
            wheel.setLayoutParams(layout);
        }

            cameraSwap = new ImageView(this);
            cameraSwap.setImageResource(R.drawable.ic_switchcamera2);
        {
            cameraSwap.setOnClickListener(switchCameraListener);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(width / 12, width / 12);
            layout.leftMargin = width - width / 12 - width /24 ;
            layout.topMargin = height - width/12-width/24;
            cameraSwap.setLayoutParams(layout);

        }

            polaroidFrame = new ImageView(myContext);
            polaroidFrame.setImageResource(R.drawable.polaroid);
            polaroidFrame.setScaleType(ImageView.ScaleType.CENTER_CROP);
            int pFrameWidth = (int)(width);
            int pFrameHeight = (int)(1.04*width);
            polaroidFrame.setRotation(-10.0f);
        {
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(pFrameHeight, pFrameHeight);
            layout.leftMargin = leftBlackoutWidth / 2;
            layout.topMargin = (int)(topBlackoutHeight/1.35);
            polaroidFrame.setLayoutParams(layout);
        }

            snapShot = new ImageView(myContext);
            snapShot.setScaleType(ImageView.ScaleType.CENTER_CROP);
            snapShot.setBackgroundColor(Color.GREEN);
            snapShot.setRotation(-10.0f);

        {
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(pFrameHeight,pFrameHeight);
            layout.leftMargin = leftBlackoutWidth / 2;
            layout.topMargin = (int)(topBlackoutHeight/1.25);
            snapShot.setLayoutParams(layout);
        }

        RotateAnimation spinWheel = new RotateAnimation(0.0f,720.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        spinWheel.setDuration(2000);
        spinWheel.setRepeatCount(spinWheel.INFINITE);
        spinWheel.setRepeatMode(spinWheel.RESTART);
        spinWheel.setInterpolator(new LinearInterpolator());

        master.addView(topBlackout);
        master.addView(leftBlackout);
        master.addView(rightBlackout);
        master.addView(bottomBlackout);
        master.addView(pictureSquare);
        master.addView(marquee);
        master.addView(wheel);
        master.addView(cameraSwap);
        wheel.startAnimation(spinWheel);


	}

	OnClickListener switchCameraListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//get the number of cameras
			int camerasNumber = Camera.getNumberOfCameras();
			if (camerasNumber > 1) {
				//release the old camera instance
				//switch camera, from the front and the back and vice versa
				
				releaseCamera();
				chooseCamera();
			} else {
				Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
				toast.show();
			}
		}
	};

	public void chooseCamera() {
		//if the camera preview is the front
		if (cameraFront) {
			int cameraId = findBackFacingCamera();
			if (cameraId >= 0) {
				//open the backFacingCamera
				//set a picture callback
				//refresh the preview
				
				mCamera = Camera.open(cameraId);				
				mPicture = getPictureCallback();			
				mPreview.refreshCamera(mCamera);
			}
		} else {
			int cameraId = findFrontFacingCamera();
			if (cameraId >= 0) {
				//open the backFacingCamera
				//set a picture callback
				//refresh the preview
				
				mCamera = Camera.open(cameraId);
				mPicture = getPictureCallback();
				mPreview.refreshCamera(mCamera);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		//when on Pause, release camera in order to be used from other applications
		releaseCamera();
	}

	private boolean hasCamera(Context context) {
		//check if the device has camera
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	private PictureCallback getPictureCallback() {
		PictureCallback picture = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				//make a new picture file
				File pictureFile = getOutputMediaFile();

				if (pictureFile == null) {
                    System.out.println("poop");
					return;
				}
				try {
					//write the file
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
					Toast toast = Toast.makeText(myContext, "Picture saved: " + pictureFile.getName(), Toast.LENGTH_LONG);
					toast.show();

				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				}

				//refresh camera to continue preview
                postPicture(pictureFile);
				mPreview.refreshCamera(mCamera);
			}
		};
		return picture;
	}

	OnClickListener captrureListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mCamera.takePicture(null, null, mPicture);
		}
	};

    private static void postPicture(File file){
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap bitmap = BitmapFactory.decodeFile(file.toString());
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        snapShot.setImageBitmap(rotatedBitmap);
        master.addView(snapShot);
        master.addView(polaroidFrame);
    }

    /*private static void postPicture(File file){
        FileInputStream in;
        BufferedInputStream buf;
        try {
            in = new FileInputStream(file.toString());
            buf = new BufferedInputStream(in);
            Bitmap bMap = BitmapFactory.decodeStream(buf);
            snapShot.setImageBitmap(bMap);
            if (in != null) {
                in.close();
            }
            if (buf != null) {
                buf.close();
            }
            System.out.println("picture done");
            master.addView(snapShot);
            master.addView(polaroidFrame);
        } catch (Exception e) {
        }
    }*/

	//make picture and save to a folder
	private static File getOutputMediaFile() {
		//make a new file directory inside the "sdcard" folder
		//File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SnapRoulette");
        File mediaStorageDir = new File("/tmp/sdcard/", "SnapRoulette");
		System.out.println("blah boo: " + mediaStorageDir);
		//if this "JCGCamera folder does not exist
		if (!mediaStorageDir.exists()) {
            System.out.println("Well shit");
			//if you cannot make this folder return
			if (!mediaStorageDir.mkdirs()) {
                System.out.println("moobar");
				return null;
			}
		}
		
		//take the current timeStamp
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        System.out.println("moobar2" + timeStamp);
		File mediaFile;
		//and make a media file:
		mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        System.out.println("moobar3" + mediaFile);
		return mediaFile;
	}

	private void releaseCamera() {
		// stop and release camera
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }
}