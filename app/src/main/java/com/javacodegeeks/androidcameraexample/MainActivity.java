package com.javacodegeeks.androidcameraexample;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.CycleInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.parse.*;
import com.facebook.FacebookSdk;
import com.parse.ParseFacebookUtils;

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
    private static View wheel;
    private static RotateAnimation spinWheel;
    private static int pFrameHeight, leftBlackoutWidth, topBlackoutHeight, width, height;
    private static View whiteOut;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		myContext = this;



		initialize();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "Qx7dQ4f0hK5bT09SUwaMJfbSB4AJYp3sGqToDwrX", "Lso4gqKfAjZAFoi1KFTbD0VcN7lWnbR8ZEQzMbMB");
        //FacebookSdk.sdkInitialize(getApplicationContext());
        ParseFacebookUtils.initialize(this);


        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();



        ParseQuery poop = new ParseQuery("TestObject");
        poop.whereEqualTo("foo","bar");
        poop.orderByDescending("updatedAt");
        poop.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    System.out.println("I got " + objects.size() + " objects.");
                    System.out.println("Object[0]: " + objects.get(0).getObjectId() + " foo: " + objects.get(0).get("foo"));
                } else {
                    System.out.println("Poop!");
                }
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
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

        AppEventsLogger.activateApp(this);
	}

	public void initialize() {
		cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
		mPreview = new CameraPreview(myContext, mCamera);
		cameraPreview.addView(mPreview);
        master = (RelativeLayout) findViewById(R.id.masterView);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
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
            topBlackoutHeight = (int)(height-width*(0.95))/2;
        {
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(topBlackoutWidth,topBlackoutHeight);
            topBlackout.setLayoutParams(layout);
            topBlackout.setAlpha(0.5f);
            topBlackout.setBackgroundColor(Color.BLACK);

        }


            View leftBlackout = new View(this);
            leftBlackoutWidth = (int) (width*(0.025));
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

            wheel = new RouletteWheel(this, 0, 0, width/6, width/6);

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
            pFrameHeight = (int)(1.04*width);
            //polaroidFrame.setRotation(-10.0f);
        {
            polaroidFrame.setOnClickListener(pictureDone);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(pFrameHeight, pFrameHeight);
            layout.leftMargin = leftBlackoutWidth / 2;
            layout.topMargin = (int)(topBlackoutHeight/1.35);
            polaroidFrame.setLayoutParams(layout);
        }

        initializeSnapShot();



        spinWheel = new RotateAnimation(0.0f,720.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
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

    public void initializeSnapShot(){
        snapShot = new ImageView(myContext);
        snapShot.setScaleType(ImageView.ScaleType.CENTER_CROP);
        snapShot.setBackgroundColor(Color.GREEN);
        //snapShot.setRotation(-10.0f);

        {
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams((int) (pFrameHeight/1.25) ,(int) (pFrameHeight/1.25));
            layout.leftMargin = (int) (4.5*leftBlackoutWidth);
            layout.topMargin = (int)(topBlackoutHeight/1.075);
            snapShot.setLayoutParams(layout);
        }
    }



	@Override
	protected void onPause() {
		super.onPause();
		//when on Pause, release camera in order to be used from other applications
		releaseCamera();

        AppEventsLogger.deactivateApp(this);
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

                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                snapShot.setImageBitmap(rotatedBitmap);

                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                data = stream.toByteArray();


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
                postPicture();
				mPreview.refreshCamera(mCamera);
			}
		};
		return picture;
	}

	OnClickListener captrureListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

            //Collection<String> foo = new Collection<String>();
            final List<String> permissions = new ArrayList<String>();
            permissions.add("public_profile");
            permissions.add("user_status");
            permissions.add("user_friends");
            ParseFacebookUtils.logInWithReadPermissionsInBackground((Activity)myContext, permissions, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    System.out.println("POOP OH MY GOD " + parseUser + " exc: " + e);
                }
            });

			mCamera.takePicture(null, null, mPicture);
		}
	};

    private void postPicture(){
        master.addView(snapShot);
        master.addView(polaroidFrame);
        wheel.clearAnimation();
        initializeWhiteOut();
    }

    private void initializeWhiteOut(){
        whiteOut = new View(this);
        Animation fadeOut = new AlphaAnimation(1f,0f);

        fadeOut.setDuration(1000);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {

            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            public void onAnimationEnd(Animation animation) {
                whiteOut.setVisibility(View.GONE);

            }
        });

        RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(width,height);
        whiteOut.setLayoutParams(layout);
        whiteOut.setBackgroundColor(Color.WHITE);
        master.addView(whiteOut);
        whiteOut.startAnimation(fadeOut);



    }

    OnClickListener pictureDone = new OnClickListener() {
        @Override
        public void onClick(View view) {

            TranslateAnimation slideDown = new TranslateAnimation(0, 0, 0, height);
            slideDown.setDuration(1000);

            slideDown.setAnimationListener(new TranslateAnimation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    master.removeView(snapShot);
                    master.removeView(polaroidFrame);
                }
            });

            snapShot.startAnimation(slideDown);
            polaroidFrame.startAnimation(slideDown);

            //master.removeView(snapShot);
            //master.removeView(polaroidFrame);

            wheel.startAnimation(spinWheel);

        }
    };

	//make picture and save to a folder
	private static File getOutputMediaFile() {
		//make a new file directory inside the "sdcard" folder
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SnapRoulette");
        //File mediaStorageDir = new File("/sdcard/", "JCG Camera");
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
}