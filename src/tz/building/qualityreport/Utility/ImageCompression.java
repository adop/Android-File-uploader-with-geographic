package tz.building.qualityreport.Utility;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;



public class ImageCompression {

	 private static final String TAG = "FileDumpUtil";
	  private static final File sDumpDirectory = new File(
			  Environment.getExternalStorageDirectory()
				.getPath() + "/QualityReport/tmp/");
//	 private static final File sDumpDirectory = new File(
//			  Environment.getExternalStorageDirectory()
//				.getPath() + "/DCIM/");
	  synchronized static public void init() {
	    // Create directory, if necessary
	    if (!sDumpDirectory.exists()) {
	      sDumpDirectory.mkdirs();
	    }
	  }

	  synchronized static public File dump(File img_file) {
		  init();
		  Bitmap bMap = BitmapFactory.decodeFile(img_file.getAbsolutePath());
	    FileOutputStream os = null;
	    File dumpFile=null;
	    try {
	      long timestamp = System.currentTimeMillis();
	      String pure_name=img_file.getName().split("\\.")[0];
	    dumpFile= new File(sDumpDirectory, timestamp+pure_name
	          + ".jpg");
	    dumpFile.createNewFile();
	      os = new FileOutputStream(dumpFile);
	      bMap.compress(CompressFormat.JPEG, 20, os);
	      os.close();
	    } catch (IOException ioe) {
	      Log.e(TAG, "GrayImage dump failed", ioe);
	    } finally {
	      try {
	        os.close();
	      } catch (Throwable t) {
	        // Ignore
	      }
	    }
		return dumpFile;
	  }

}
