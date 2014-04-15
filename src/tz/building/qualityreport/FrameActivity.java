package tz.building.qualityreport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.entity.*;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import tz.building.qualityreport.Data.FileContent;
import tz.building.qualityreport.Utility.ImageCompression;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;

import android.support.v4.app.FragmentManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;
import android.view.*;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class FrameActivity extends Activity {
	private List<File> sfileList = new ArrayList<File>();

	private FrameActivity currentActivity = this;
	private String filePath = Environment.getExternalStorageDirectory()
			.getPath() + "/QualityReport/archive/";
	// "/sdcard/QualityReport/archive/";
	private List<String> gFileList = new ArrayList<String>();
	private StableArrayAdapter adapter;

	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	int userID;
	private Builder sendingProgressDialogBuilder;
	private AlertDialog sendingProgressDialog;
	//private static final String serviceUrl="http://192.168.0.138/workflowmanager/services/services.php?";
	private static final String serviceUrl="http://dev.plutoless.com/workflowmanager/services/services.php?";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		Intent myIntent = getIntent(); // gets the previously created intent
		// userID = Integer.parseInt(myIntent.getStringExtra("userID")); // will
		// return "FirstKeyValue"
		sendingProgressDialogBuilder=new AlertDialog.Builder(currentActivity)
		.setMessage("发送中")
		.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						// continue with delete
						dialog.dismiss();
					}
				});
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		userID =prefs.getInt("user_id", -1);
		setContentView(R.layout.activity_frame);

		final ListView listview = (ListView) findViewById(R.id.listview);

		createDir();

		mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
		mLocationClient
				.registerLocationListener((BDLocationListener) myListener);
		setLocationPara();
		mLocationClient.start();
		File[] files = new File(filePath).listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					gFileList.add(file.getName());
				}
			}
		}

		adapter = new StableArrayAdapter(this,
				android.R.layout.simple_list_item_multiple_choice, gFileList);
		listview.setAdapter(adapter);



		// implement create function

		findViewById(R.id.button_create).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						openFileCreateDialog();

					}

					private void openFileCreateDialog() {
			
						AlertDialog.Builder builder = new AlertDialog.Builder(
								currentActivity);
						builder.setTitle(R.string.pick_file_type);
						builder.setItems(R.array.file_type_array,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int item) {
										// //
										// Toast.makeText(getApplicationContext(),
										// items[item],
										// Toast.LENGTH_SHORT).show();
										createAndOpenFile(item);
									}
								}).show();
					}
				});

		
		// Make the button disabled when multiple files are selected;

		View button_o = findViewById(R.id.button_open);
		button_o.setOnClickListener(

		//
		new View.OnClickListener() {
			@SuppressLint("NewApi")
			@Override
			public void onClick(View view) {
				int selectItemNumber = getListView().getCheckedItemCount();
				if (selectItemNumber > 1) {
					new AlertDialog.Builder(currentActivity)
							.setMessage("请选择单个文件")
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											// continue with delete
											dialog.dismiss();
										}
									}).show();
					return;
				}
				if (selectItemNumber == 1) {
					openSelectedFile();
					return;
				}
				if (selectItemNumber < 1) {
					new AlertDialog.Builder(currentActivity)
							.setMessage("请选择一个文件")
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											// continue with delete
											dialog.dismiss();
										}
									}).show();
					return;
				}

			}

		});
		// implement send function
		findViewById(R.id.button_send).setOnClickListener(
				new View.OnClickListener() {
					@SuppressLint("NewApi")
					@Override
					public void onClick(View view) {
						int selectItemNumber = getListView()
								.getCheckedItemCount();
						if (selectItemNumber < 1) {
							new AlertDialog.Builder(currentActivity)
									.setMessage("请选择至少一个文件")
									.setPositiveButton(
											android.R.string.yes,
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int which) {
													// continue with delete
													dialog.dismiss();
												}
											}).show();
							return;
						}

						new SendFileTask().execute();
						
					}

				});
	}
	class SendFileTask extends AsyncTask<Void, Void, Boolean> {

	    private Exception exception;
	    


		@Override
		protected void onPostExecute(final Boolean success) {
	        // TODO: check this.exception 
	        // TODO: do something with the feed
			//showProgress(false);
			new AlertDialog.Builder(currentActivity)
			.setMessage("发送完毕")
			.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int which) {
							// continue with delete
							dialog.dismiss();
						}
					}).show();
	    }

		@Override
		protected Boolean doInBackground(Void... params) {
			 try {
				// showProgress(true);
				 sendFiles();
				 return true;
		         
		        } catch (Exception e) {
		            this.exception = e;
		            return false;
		        }
		}
		
		@Override
		protected void onCancelled() {
			
			//showProgress(false);
		}
	}
	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		super.onResume(); // Always call the superclass method first
		final ListView view = getListView();
		// uncheckAllChildrenCascade(view);

		view.animate().setDuration(2000).alpha(0).withEndAction(new Runnable() {
			@SuppressLint("NewApi")
			@Override
			public void run() {

				adapter.notifyDataSetChanged();
				view.setAlpha(1);
			}
		});
	}

	private void createDir() {
		File theDir = new File(filePath);

		// if the directory does not exist, create it
		if (!theDir.exists()) {

			boolean result = theDir.mkdirs();

			if (result) {
				System.out.println("DIR created");
			}
		}
	}

	private ListView getListView() {
		return (ListView) findViewById(R.id.listview);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.frame, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		mLocationClient.stop();

		super.onDestroy();
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {


		switch (item.getItemId()) {
		case R.id.action_delete:
			// startActivity(new Intent(this, About.class));
			// TODO delete selected items
			long[] fileIDs = getListView().getCheckedItemIds();

			if (fileIDs.length == 0) {
				break;

			}
			String[] fileNames = new String[fileIDs.length];
			int i = 0;
			for (long id : fileIDs) {
				fileNames[i] = (gFileList.get((int) id));
				i++;

			}
			for (String fn : fileNames) {
				removeFile(fn);
			}

			final ListView view = getListView();
			uncheckAllChildrenCascade(view);

			view.animate().setDuration(2000).alpha(0)
					.withEndAction(new Runnable() {
						@SuppressLint("NewApi")
						@Override
						public void run() {

							adapter.notifyDataSetChanged();
							view.setAlpha(1);
						}
					});
			break;

		case R.id.action_import:
			// File mPath = new File(Environment.getExternalStorageDirectory() +
			// "//DIR//");
			File mPath = new File(Environment.getExternalStorageDirectory()
					.toString());
			FileDialog fileDialog = new FileDialog(this, mPath);
			// fileDialog.setFileEndsWith(".txt");
			fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
				public void fileSelected(File file) {

					copyFile(file);
					addFileAndRefreshView(file);

					// Log.d(getClass().getName(), "selected file " +
					// file.toString());
				}

			});

			fileDialog.showDialog();
			break;
			
			case R.id.action_exit:
				Editor editor =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
				editor.clear();
				editor.commit();
				
				finish();
		default:

		}
		return super.onOptionsItemSelected(item);
	}

	private void copyFile(File file) {
		// TODO Auto-generated method stub
		InputStream inStream = null;
		OutputStream outStream = null;

		try {

			File afile = file;
			File bfile = new File(filePath + file.getName());

			inStream = new FileInputStream(afile);
			outStream = new FileOutputStream(bfile);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0) {

				outStream.write(buffer, 0, length);

			}

			inStream.close();
			outStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean sendFiles() {

		String rs[] = requestLocation();
		String time = rs[0];
		String latitude = rs[1];
		String longtitude = rs[2];
		String address = rs[3];

		long[] fileIDs = getListView().getCheckedItemIds();
		sfileList.clear();
		for (long id : fileIDs) {
			sfileList.add(new File(filePath + gFileList.get((int) id)));
		}
		// TODO send sfileList to server
		for (File file : sfileList) {
			postFileToServer(file, time, latitude, longtitude, address);
		}
		return true;
	}
	
	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		
		if(show) {
			sendingProgressDialog=sendingProgressDialogBuilder.show();}
		else
			{if(sendingProgressDialog!=null)
				sendingProgressDialog.dismiss();}
	}

	private void postFileToServer(File file, String time, String latitude,
			String longtitude, String address) {
		// TODO Auto-generated method stub
		
		HttpClient client = new DefaultHttpClient();
		
		
		 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	      nameValuePairs.add(new BasicNameValuePair("pos",
	          address));
	      nameValuePairs.add(new BasicNameValuePair("uid",
	    		  String.valueOf(userID)));
	      nameValuePairs.add(new BasicNameValuePair("text", "text area"));
		String fileName = file.getAbsolutePath();
		if (fileName.endsWith(".png") || fileName.endsWith(".jpg")
				|| fileName.endsWith(".bmp")) {
			HttpPost post = new HttpPost(
					serviceUrl+"action=3");

			MultipartEntity entity = new MultipartEntity();
			try {

				entity.addPart("text",new StringBody("text area", Charset.forName("UTF-8")));
				entity.addPart("pos",new StringBody(address, Charset.forName("UTF-8")));
				entity.addPart("uid",new StringBody(String.valueOf(userID), Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			 File tmpFile=ImageCompression.dump(file);
			FileBody fileBody = new FileBody(tmpFile);
			entity.addPart("attachment", fileBody);
			post.setEntity(entity);

			HttpResponse response;
			try {
				response = client.execute(post);
				HttpEntity responsEntity = response.getEntity();

				StatusLine st= response.getStatusLine(); // CONSIDER Detect server
				// complaints
				
				InputStream content =responsEntity.getContent();
				InputStreamReader is = new InputStreamReader(content);
				BufferedReader br = new BufferedReader(is,2000);

				String read = br.readLine();
				
				String responseRst = read;
				while (read != null){
				    Log.e("AUTH", read);
				    read = br.readLine();
				    responseRst += read + "\n";
				}
				if(responseRst!=null){}
				responsEntity.consumeContent();
				client.getConnectionManager().shutdown();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return;

		} else if (fileName.endsWith(".rpta")) {
			FileContent sfc = readObj(file.getAbsolutePath());


			addPostParam(file, nameValuePairs, sfc);

		}

		else if (fileName.endsWith(".rptb")) {
			FileContent sfc = readObj(file.getAbsolutePath());


			addPostParam(file, nameValuePairs, sfc);
		}

		else if (fileName.endsWith(".rptc")) {
			FileContent sfc = readObj(file.getAbsolutePath());

//	
			addPostParam(file, nameValuePairs, sfc);
		} else {
			return;
		}
		HttpPost post = new HttpPost(
				serviceUrl+"action=4");

		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			
			HttpResponse response = client.execute(post);
			HttpEntity responsEntity = response.getEntity();

			StatusLine st= response.getStatusLine(); // CONSIDER Detect server
			// complaints
			
			InputStream content =responsEntity.getContent();
			InputStreamReader is = new InputStreamReader(content);
			BufferedReader br = new BufferedReader(is,2000);

			String read = br.readLine();
			
			String responseRst = read;
			while (read != null){
			    Log.e("AUTH", read);
			    read = br.readLine();
			    responseRst += read + "\n";
			}
			if(responseRst!=null){}
			responsEntity.consumeContent();
			client.getConnectionManager().shutdown();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addPostParam(File file, List<NameValuePair> nameValuePairs,
			FileContent sfc) {
		 long timestamp = System.currentTimeMillis();
		nameValuePairs.add(new BasicNameValuePair("title", timestamp+file.getName()
				));
		nameValuePairs.add(new BasicNameValuePair("var1", sfc.title
				));
		nameValuePairs.add(new BasicNameValuePair("var2", sfc.body
				));
		nameValuePairs.add(new BasicNameValuePair("var3", sfc.end
				));
	}

	private String[] requestLocation() {

		if (mLocationClient != null && mLocationClient.isStarted())
			mLocationClient.requestLocation();
		while (((MyLocationListener) myListener).getResult() == null) {
			// wait
		}
		String result = ((MyLocationListener) myListener).getResult();
		String rs[] = result.split(";");

		return rs;
	}

	private void setLocationPara() {
		// TODO Auto-generated method stub
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd0911");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(5000);// 设置发起定位请求的间隔时间为500ms
		option.disableCache(true);// 禁止启用缓存定位
		option.setPoiNumber(5); // 最多返回POI个数
		option.setPoiDistance(1000); // poi查询距离
		option.setPoiExtraInfo(true); // 是否需要POI的电话和地址等详细信息
		mLocationClient.setLocOption(option);
	}

	private boolean openSelectedFile() {
		long[] fileIDs = getListView().getCheckedItemIds();
		File file = new File(filePath + gFileList.get((int) fileIDs[0]));
		openFile(file);
		return true;
	}

	@SuppressLint("NewApi")
	private boolean createAndOpenFile(int type) {
		String fileSuffix = null;
		if (type == 0) {
			fileSuffix = ".rpta";
		} else if (type == 1) {
			fileSuffix = ".rptb";
		} else if (type == 2) {
			fileSuffix = ".rptc";
		}
		java.util.Date date = new java.util.Date();
		String fileName = (new Timestamp(date.getTime())).toString()
				.replaceAll("\\s+", "").replaceAll("-", "").replaceAll(":", "")
				.replaceAll("\\.", "")
				+ fileSuffix;
		File file = new File(filePath + fileName);
		try {

			boolean suc = file.createNewFile();
			writeEmptyObj(file.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		addFileAndRefreshView(file);
		openFile(file);
		return true;
	}

	private void writeEmptyObj(String fileName) {
		// TODO Auto-generated method stub

		try {
			FileOutputStream fileOut = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(new FileContent());
			out.close();
			fileOut.close();

		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	@SuppressLint("NewApi")
	private void addFileAndRefreshView(File file) {
		gFileList.add(file.getName());
		final ListView view = getListView();
		view.animate().setDuration(2000).alpha(0).withEndAction(new Runnable() {
			@SuppressLint("NewApi")
			@Override
			public void run() {

				adapter.notifyDataSetChanged();
				view.setAlpha(1);
			}
		});
	}

	private void openFile(File file) {
		// TODO open file and load obj
		String fileName = file.getAbsolutePath();
		if (fileName.endsWith(".png") || fileName.endsWith(".jpg")
				|| fileName.endsWith(".bmp")) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + fileName), "image/*");
			startActivity(intent);

		} else if (fileName.endsWith(".rpta")) {
			openTypeAFile(file);

		}

		else if (fileName.endsWith(".rptb")) {
			openTypeBFile(file);

		}

		else if (fileName.endsWith(".rptc")) {
			openTypeCFile(file);

		} else {
			new AlertDialog.Builder(currentActivity)
					.setMessage("无法打开该类型文件")
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// continue with delete
									dialog.dismiss();
								}
							}).show();
		}
	}

	private void openTypeCFile(File file) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, OpenTypeC.class);
		String path = file.getAbsolutePath();
		Bundle b = new Bundle();
		b.putString("name", path);

		intent.putExtras(b);

		startActivity(intent);
	}

	private void openTypeBFile(File file) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, OpenTypeB.class);
		String path = file.getAbsolutePath();
		Bundle b = new Bundle();
		b.putString("name", path);

		intent.putExtras(b);

		startActivity(intent);

	}

	private void openTypeAFile(File file) {
		// TODO Auto-generated method stub

		Intent intent = new Intent(this, OpenTypeA.class);
		String path = file.getAbsolutePath();
		Bundle b = new Bundle();
		b.putString("name", path);

		intent.putExtras(b);

		startActivity(intent);
	}

	@SuppressLint("NewApi")
	private void removeFile(String fileName) {
		gFileList.remove(fileName);
		File file = new File(filePath + fileName);
		file.delete();

	}

	private FileContent readObj(String fileName) {
		// TODO Auto-generated method stub

		FileContent mfc = null;
		try {
			FileInputStream fileIn = new FileInputStream(fileName);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			mfc = (FileContent) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			System.out.println("Employee class not found");
			c.printStackTrace();
			return null;
		}
		return mfc;
	}

	private void uncheckAllChildrenCascade(ListView vw) {

		int len = vw.getCount();
		SparseBooleanArray checked = vw.getCheckedItemPositions();
		for (int i = 0; i < len; i++)
			if (checked.get(i)) {
				vw.setItemChecked(i, false);

			}
	}

	private class StableArrayAdapter extends ArrayAdapter<String> {

		// HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			// for (int i = 0; i < objects.size(); ++i) {
			// mIdMap.put(objects.get(i), i);
			// }
		}

		

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}

}

class FileTypeSelectDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.pick_file_type);
		builder.setItems(R.array.file_type_array,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// The 'which' argument contains the index position
						// of the selected item
					}
				});
		// Create the AlertDialog object and return it
		return builder.create();
	}
}