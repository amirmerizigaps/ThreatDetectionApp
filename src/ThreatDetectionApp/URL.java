package ThreatDetectionApp;

public class URL extends input{
	private String Uformat;

	public URL(int ID, String Content, String Uformat) {
		super(ID,Content);
		
		this.Uformat=Uformat;
	}

	public String getUformat() {
		
		return Uformat;
		
	}
	public void setUrlFormat(String urlFormat) {
		
	}
}
