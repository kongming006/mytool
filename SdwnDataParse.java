package mytools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.*;

enum DataType {
	IPERF_TCP_BANDWIDTH, IPERF_UDP_BANDWIDTH, TERMINAL_PING
}

/**
 * SDWN��Ŀ���ݽ������ߣ����Խ���iperf���Ե����ݣ�Ҳ���Խ���ping���Ե�����
 * ������ʽ���������������ɣ���ַ��http://www.txt2re.com/index.php3
 * @author kong
 *
 */
public class SdwnDataParse {

	public static void main(String[] args) {
		
		String filePath = "C:\\Users\\kong\\Desktop\\��ͨAP\\ʱ�Ӳ���\\sta_to_controller_ping.txt";
		DataType fileType = DataType.TERMINAL_PING;
		
		try
	    {
	        File file = new File(filePath);
	        
	        // �ж��ļ��Ƿ����
	        if (file.isFile() && file.exists())
	        { 
	            InputStreamReader read = new InputStreamReader(
	                    new FileInputStream(file));
	            BufferedReader bufferedReader = new BufferedReader(read);
	            String line = null;
	            
	            while ((line = bufferedReader.readLine()) != null)
	            {
	            	String result = parseData(line, fileType);
	            	if (result != null && !result.equals("")) {
	            		System.out.println(result);
	            	}
	            }
	            bufferedReader.close();
	            read.close();
	        }
	        else
	        {
	            System.out.println("�Ҳ���ָ�����ļ�");
	        }
	    }
	    catch (Exception e)
	    {
	        System.out.println("��ȡ�ļ����ݳ���");
	        e.printStackTrace();
	    }
	}
	
	/**
	 * �������ݵĽӿں���
	 * @param txt ��������������
	 * @param type ����������������
	 * @return
	 */
	public static String parseData(String txt, DataType type) {
		String result = null;
		switch(type) {
		case IPERF_TCP_BANDWIDTH:
			result = parseIperfTcpBandwidth(txt);
			break;
		case IPERF_UDP_BANDWIDTH:
			result = parseIperfUdpBandwidth(txt);
			break;
		case TERMINAL_PING:
			result = parsePingLatency(txt);
			break;
		default:
			break;
		}
		return result;
	}
	
	/**
	 * ��iperf����TCP����������ļ��н�����Bandwidth��һ������
	 * @param txt
	 */
	public static String parseIperfTcpBandwidth(String txt) {
		String re1=".*?";	// Non-greedy match on filler
	    String re2="[+-]?\\d*\\.\\d+(?![-+0-9\\.])";	// Uninteresting: float
	    String re3=".*?";	// Non-greedy match on filler
	    String re4="[+-]?\\d*\\.\\d+(?![-+0-9\\.])";	// Uninteresting: float
	    String re5=".*?";	// Non-greedy match on filler
	    String re6="([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 1

	    Pattern p = Pattern.compile(re1+re2+re3+re4+re5+re6,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m = p.matcher(txt);
	    if (m.find()) {
	        String float1=m.group(1);
	        return float1;
	    }
	    else {
	    	return null;
	    }
	}
	
	/**
	 * ��iperf����UDP����������ļ��н�����Bandwidth��һ������
	 * @param txt
	 */
	public static String parseIperfUdpBandwidth(String txt) {
	    String re1=".*?";	// Non-greedy match on filler
	    String re2="[+-]?\\d*\\.\\d+(?![-+0-9\\.])";	// Uninteresting: float
	    String re3=".*?";	// Non-greedy match on filler
	    String re4="[+-]?\\d*\\.\\d+(?![-+0-9\\.])";	// Uninteresting: float
	    String re5=".*?";	// Non-greedy match on filler
	    String re6="([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";	// Float 1

	    Pattern p = Pattern.compile(re1+re2+re3+re4+re5+re6,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m = p.matcher(txt);
	    if (m.find()) {
	        String float1=m.group(1);
	        return float1;
	    }
	    else {
	    	return null;
	    }
	}
	
	/**
	 * ��ping���Ե������ļ��н�����ʱ������
	 * @param txt
	 */
	public static String parsePingLatency(String txt) {
		String re1=".*?";	// Non-greedy match on filler
	    String re2="\\d+";	// Uninteresting: int
	    String re3=".*?";	// Non-greedy match on filler
	    String re4="\\d+";	// Uninteresting: int
	    String re5=".*?";	// Non-greedy match on filler
	    String re6="\\d+";	// Uninteresting: int
	    String re7=".*?";	// Non-greedy match on filler
	    String re8="\\d+";	// Uninteresting: int
	    String re9=".*?";	// Non-greedy match on filler
	    String re10="\\d+";	// Uninteresting: int
	    String re11=".*?";	// Non-greedy match on filler
	    String re12="\\d+";	// Uninteresting: int
	    String re13=".*?";	// Non-greedy match on filler
	    String re14="\\d+";	// Uninteresting: int
	    String re15=".*?";	// Non-greedy match on filler
	    String re16="(\\d+)";	// Integer Number 1
	    String re17="(.)";	// Any Single Character 1
	    String re18="(\\d+)";	// Integer Number 2

	    Pattern p = Pattern.compile(re1+re2+re3+re4+re5+re6+re7+re8+re9+re10+re11+re12+re13+re14+re15+re16+re17+re18,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m = p.matcher(txt);
	    if (m.find()) {
	        String int1=m.group(1);
	        String c1=m.group(2);
	        String int2=m.group(3);
	        return int1.toString()+c1.toString()+int2.toString();
	    }
	    else {
	    	return null;
	    }
	}

}