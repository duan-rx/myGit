package constant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class convertStream {
	public static String readFromInputStream(InputStream in) throws IOException {
	      ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    byte[] buffer = new byte[1024];
	      int len = -1;
	      while ((len = in.read(buffer)) != -1) {
	          baos.write(buffer, 0, len);
	      }
	      baos.close();
	      in.close();
	    
	     byte[] lens = baos.toByteArray();
	     String result = new String(lens,"UTF-8");//ฤฺศÝยาย๋ดฆภํ
	     
	     return result;
	 }


}
