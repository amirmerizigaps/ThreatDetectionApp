package ThreatDetectionApp;

public class URL extends input{
	private String uformat;

	public URL(int ID, String Content, String uformat) {
		super(ID,Content);
		
		this.uformat=uformat;
	}

	public String getUformat() {
		
		return uformat;
		
	}
	public void setUFormat(String uFormat) {
	    this.uformat = uFormat;
	}
}
