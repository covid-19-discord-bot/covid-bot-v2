import java.io.Serializable;

public class AutoUpdateData implements Serializable {
  private static final long serialVersionUID = 6036616811273256211L;
  
  public int interval;
  
  public String country;
  
  public AutoUpdateData(int t, String s) {
    this.interval = t;
    this.country = s;
  }
}

