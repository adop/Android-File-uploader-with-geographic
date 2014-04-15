package tz.building.qualityreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tz.building.qualityreport.Data.FileContent;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.EditText;

public class OpenTypeA extends Activity {
	FileContent fc;
	String fileName;
	private boolean no_back_key;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_type_a);
		Bundle b = getIntent().getExtras();
//		String title = b.getString("title");
//		String body = b.getString("body");
//		String end = b.getString("end");
		fileName=b.getString("name");
		File file=new File(fileName);
		
		fc=readObj(fileName);
		if(fc==null){
			openError();
			return;
		}
		 EditText editTextTitle = (EditText) findViewById(R.id.a_title);
		 editTextTitle.setText(fc.title);
		  
		 EditText editTextBody = (EditText) findViewById(R.id.a_body);
		 editTextBody.setText(fc.body);
		 
		 EditText editTextEnd = (EditText) findViewById(R.id.a_end);
		 editTextEnd.setText(fc.end);
	}

	private void openError() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(this)
		.setMessage("无法打开文件"+fileName)
		.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(
							DialogInterface dialog,
							int which) {
						// continue with delete
						dialog.dismiss();
						finish();
					}
				}).show();
	}

	private FileContent readObj(String fileName) {
		// TODO Auto-generated method stub
		
		FileContent mfc= null;
	      try
	      {
	         FileInputStream fileIn = new FileInputStream(fileName);
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         mfc = (FileContent) in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException i)
	      {
	         i.printStackTrace();
	         return null ;
	      }catch(ClassNotFoundException c)
	      {
	         System.out.println("Employee class not found");
	         c.printStackTrace();
	         return null;
	      }
		return mfc;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.open_type, menu);
		return true;
	}
	
//	@Override
//	  public void onBackPressed() {
//	             //Here you get Back Key Press So make boolean false
//	             no_back_key=false;
//	             //super.onBackPressed();
//	     	    new AlertDialog.Builder(this)
//	    		.setMessage("保存文件")
//	    		.setPositiveButton(android.R.string.yes,
//	    				new DialogInterface.OnClickListener() {
//	    					public void onClick(
//	    							DialogInterface dialog,
//	    							int which) {
//	    						saveFile();
//	    						//dialog.dismiss();
//	    					}
//
//
//	    				})
//	    				.setNegativeButton(android.R.string.no,
//	    				new DialogInterface.OnClickListener() {
//	    					public void onClick(
//	    							DialogInterface dialog,
//	    							int which) {
//	    						dialog.dismiss();
//	    					}
//
//
//	    				})
//	    				.show();
//	    	    
//	    	    finish();
//	} 
	
	private  void exit(){
	    this.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

	        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
	       // alertbox.setTitle("Message");
	        alertbox.setMessage("保存文件? ");

	        alertbox.setPositiveButton("取消  ",
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface arg0, int arg1) {
	                    	
	                    }
	                });

	        alertbox.setNeutralButton("否",
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface arg0, int arg1) {
	                    	exit();
	                    }
	                });
	        alertbox.setNegativeButton("是 ",
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface arg0, int arg1) {
	                    	saveFile();
	                        exit();
	                    	
	                    }
	                });
	        alertbox.show();

	        return true;
	    } else {
	        return super.onKeyDown(keyCode, event);
	    }

	}
	
	@Override
	public void onPause() {
	    super.onPause();  // Always call the superclass method first

	}
	//TODO onResume
	private void writeObj() {
		// TODO Auto-generated method stub
		
	      try
	      {
	         FileOutputStream fileOut =
	         new FileOutputStream(fileName);
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(fc);
	         out.close();
	         fileOut.close();
	        
	      }catch(IOException i)
	      {
	          i.printStackTrace();
	      }
	}
	private void saveFile() {
		// TODO Auto-generated method stub
		EditText et=(EditText) findViewById(R.id.a_title);
		String ett=et.getText().toString();
		fc.title=ett;
		//fc.title=((EditText) findViewById(R.id.a_title)).getText().toString();
	    fc.body=((EditText) findViewById(R.id.a_body)).getText().toString();
	    fc.end=((EditText) findViewById(R.id.a_end)).getText().toString();
		
	    writeObj();
	}
}
