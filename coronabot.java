import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class coronabot {
  public static void main(String[] args) {
    new JDAevents();
  }
  
  public static void convert() {
    HashMap<Long, AutoUpdateData> channelsToAuto = new HashMap<>();
    try {
      FileInputStream fis = new FileInputStream("guildAutoUpdateData.dat");
      ObjectInputStream ois = new ObjectInputStream(fis);
      ArrayList<Long> temp = (ArrayList<Long>)ois.readObject();
      for (Long l : temp)
        channelsToAuto.put(l, new AutoUpdateData(2, "World")); 
      channelsToAuto = (HashMap<Long, AutoUpdateData>)ois.readObject();
      ois.close();
      fis.close();
    } catch (IOException|ClassNotFoundException e) {
      e.printStackTrace();
    } 
    try {
      FileOutputStream fos = new FileOutputStream("guildAutoUpdateData.dat");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(channelsToAuto);
      oos.close();
      fos.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } 
  }
}

