package mobi.threeam.npang;

import android.app.Application;
import android.media.SoundPool;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import mobi.threeam.npang.database.DBHelper;

//@ReportsCrashes(formKey = "", mailTo = BambooConfig.ADMIN_EMAIL, mode = ReportingInteractionMode.TOAST, resToastText = R.string.acra_toast_msg)
public class NPangApp extends Application {
	private static NPangApp app;
	public static final boolean DEBUG = true;

	protected DBHelper dbHelper;

	protected SoundPool mSoundPool;
	public int soundId;

	public DBHelper getHelper() {
		if (dbHelper == null) {
			dbHelper = OpenHelperManager.getHelper(this, DBHelper.class);
		}
		return dbHelper;
	}

	@Override
	public void onCreate() {
//		ACRA.init(this);
		super.onCreate();
		app = this;
		
		getHelper();
		
//		initImageLoader();
	}
	
//	private void initImageLoader() {
//		DisplayMetrics display = getApplicationContext().getResources().getDisplayMetrics();
//
//		DisplayImageOptions options = new DisplayImageOptions.Builder()
////		 .showImageOnLoading(R.drawable.ic_stub)
////	        .showImageForEmptyUri(R.drawable.ic_empty)
////	        .showImageOnFail(R.drawable.ic_error)
//	        .resetViewBeforeLoading(true)
////	        .delayBeforeLoading(1000)
//	        .cacheInMemory(true)
//	        .cacheOnDisc(false) // default
////	        .preProcessor(...)
////	        .postProcessor(...)
////	        .extraForDownloader(...)
////	        .considerExifParams(false) // default
////	        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
////	        .bitmapConfig(Bitmap.Config.ARGB_8888) // default
////	        .decodingOptions(...)
////	        .displayer(new SimpleBitmapDisplayer()) // default
////	        .handler(new Handler()) // default
//	        .build();
//
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
//			.memoryCacheExtraOptions(display.widthPixels, display.heightPixels)
//			.defaultDisplayImageOptions(options)
//			.build();
//        ImageLoader.getInstance().init(config);
//	}
	
	public static NPangApp get() {
		return app;
	}
}
