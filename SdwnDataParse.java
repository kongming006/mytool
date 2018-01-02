package mytools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.*;

/**
 * SDWN项目数据解析工具，可以解析iperf测试的数据，也可以解析ping测试的数据
 * 正则表达式由在线生成器生成，网址：http://www.txt2re.com/index.php3
 * @author kong
 *
 */
public class SdwnDataParser {
	
	private File file = null;
	private InputStreamReader isr = null;
	private BufferedReader br = null;

	public SdwnDataParser(File file) {
		this.file = file;
	}
	
	/**
	 * 读取文件中的一行测试数据
	 * @return
	 * @throws IOException
	 */
	public String readLine() throws IOException {
		
		String line = null;
		
		if (br == null) {
			try {
				isr = new InputStreamReader(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
            br = new BufferedReader(isr);
		}
		
		if ((line = br.readLine()) != null) {
			return line;
		}
		else {
			br.close();
			isr.close();
			return null;
		}
	}
	
	/**
	 * 从iperf测试带宽的数据文件中解析出Bandwidth这一列数据，UDP和TCP一样
	 * @param txt
	 */
	public String parseIperfBandwidth(String txt) {
		String re1 = "[0-9\\.]+(?=\\sMbits/sec)";

	    Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m = p.matcher(txt);
	    if (m.find()) {
	        String float1=m.group();
	        return float1;
	    }
	    else {
	    	return null;
	    }
	}
	
	/**
	 * 从iperf测试UDP的数据文件中解析出Jitter这一列数据
	 * @param txt
	 */
	public String parseIperfUdpJitter(String txt) {
	    
		String re1 = "[0-9\\.]+(?=\\sms)";

	    Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m = p.matcher(txt);
	    
	    if (m.find()) {
	        String float1=m.group();
	        return float1;
	    }
	    else {
	    	return null;
	    }
	}
	
	/**
	 * 从iperf测试UDP的数据文件中解析出Datagrams这一列数据
	 * @param txt
	 */
	public String parseIperfUdpDatagrams(String txt) {
	    
		String re1 = "(?<=\\()[0-9\\.]+%(?=\\))";

	    Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m = p.matcher(txt);
	    if (m.find())
	    {
	        String rbraces1=m.group();
	        return rbraces1;
	    }
	    else {
	    	return null;
	    }
	}
	
	/**
	 * 从ping测试的数据文件中解析出时延数据
	 * @param txt
	 */
	public String parsePingLatency(String txt) {
		String re1 = "(?<=time=)[0-9\\.]+(?=\\sms)";

	    Pattern p = Pattern.compile(re1,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m = p.matcher(txt);
	    if (m.find()) {
	        String res = m.group();
	        return res;
	    }
	    else {
	    	return null;
	    }
	}

	
	public static void main(String[] args) {
		
		String filePath = "E:\\实验室项目\\SDWN项目\\测试数据\\跨信道UDP上行测试\\跨信道UDP上行测试2.txt";
		File file = new File(filePath);
		if (!file.exists() || !file.isFile())
			return;
		
		SdwnDataParser sdp = new SdwnDataParser(file); 
		String line;
		try {
			while ((line = sdp.readLine()) != null ) {
				String result = sdp.parseIperfUdpDatagrams(line);
				if (result != null && !result.equals("")) {
					System.out.println(result);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
