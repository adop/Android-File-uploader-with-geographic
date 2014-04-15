package tz.building.qualityreport;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

public class MyLocationListener implements BDLocationListener {

	private String result_String=null;
	
	public void resetResult(){
		result_String=null;
	}
	
	public String getResult(){
		return result_String;
	}
	public void onReceiveLocation(BDLocation location) {
		String time;
		double latitude;
		double longtitude;
		String addr = "";
		if (location == null)
			return ;
		StringBuffer sb = new StringBuffer(256);
		sb.append("time : ");
		 time=location.getTime();
		sb.append(time);
		sb.append("\nerror code : ");
		sb.append(location.getLocType());
		sb.append("\nlatitude : ");
		latitude=location.getLatitude();
		sb.append(latitude);
		sb.append("\nlontitude : ");
		longtitude=location.getLongitude();
		sb.append(longtitude);
		sb.append("\nradius : ");
		sb.append(location.getRadius());
		if (location.getLocType() == BDLocation.TypeGpsLocation){
			sb.append("\nspeed : ");
			sb.append(location.getSpeed());
			sb.append("\nsatellite : ");
			sb.append(location.getSatelliteNumber());
			sb.append("\naddr : ");
			addr=location.getAddrStr();
			sb.append(addr);
		} else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
			sb.append("\naddr : ");
			addr=location.getAddrStr();
			sb.append(addr);
		} 
		
		
		
		result_String=time+";"+latitude+";"+longtitude+";"+addr;
		//logMsg(sb.toString());
	}
public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null){
				return ;
			}
			StringBuffer sb = new StringBuffer(256);
			sb.append("Poi time : ");
			sb.append(poiLocation.getTime());
			sb.append("\nerror code : ");
			sb.append(poiLocation.getLocType());
			sb.append("\nlatitude : ");
			sb.append(poiLocation.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(poiLocation.getLongitude());
			sb.append("\nradius : ");
			sb.append(poiLocation.getRadius());
			if (poiLocation.getLocType() == BDLocation.TypeNetWorkLocation){
				sb.append("\naddr : ");
				sb.append(poiLocation.getAddrStr());
			} 
			if(poiLocation.hasPoi()){
				sb.append("\nPoi:");
				sb.append(poiLocation.getPoi());
			}else{				
				sb.append("noPoi information");
			}
			//logMsg(sb.toString());
		}
}