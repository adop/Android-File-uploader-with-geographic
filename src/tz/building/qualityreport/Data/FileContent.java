package tz.building.qualityreport.Data;

public class FileContent  implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String title;
	public String body;
	public String end;
	public FileContent(){
		this.title=" ";
		this.body=" ";
		this.end=" ";
	}
}
