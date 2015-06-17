import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;


public class CrackMD5 extends Thread {
	static private String goal;
	static private char[] codeArray;
	static AtomicInteger cntTest;
	static int cntSearchSpace = 0;
	static int testStrLength;
	
	static {
		codeArray = new char[26 * 2 + 10];
		for (int i = 0; i < 10; i++)
			codeArray[i] = (char) ('0' + i);
		for (int i = 10; i < 10 + 26; i++)
			codeArray[i] = (char) ('a' + i - 10);
		for (int i = 36; i < 36 + 26; i++)
			codeArray[i] = (char) ('A' + i - 36);
		
		cntTest = new AtomicInteger(0);
	}
	
	private String md5;
	MessageDigest md;
	int partitionStart;
	int partitionEnd;
	

	
	public CrackMD5(String msg) {
		goal = msg;
	}
	
	
	/**
	 * Single thread version of cracking
	 * @param l
	 */
	public static void crackString(int l) {
		testStrLength = l;
		char[] raw = new char[testStrLength];
		cntSearchSpace = (int) Math.pow(codeArray.length, l);
		System.out.printf("Search space size %d.\n", cntSearchSpace);
		
		CrackMD5 c = new CrackMD5("5d41402abc4b2a76b9719d911017c592");
		c.testStringHelper(raw, 0);
	}
	
	
	/**
	 * Multi-threading version of cracking
	 * @param l
	 * @param tc
	 */
	public static void crackString_mt(int l, int tc) {
		testStrLength = l;
		cntSearchSpace = (int) Math.pow(codeArray.length, l);
		System.out.printf("Search space size %d.\n", cntSearchSpace);
		
		
		LinkedList<Thread> ths = new LinkedList<>();
		int partitionSize = codeArray.length / tc;
		for (int i = 0; i < tc; i++) {
			CrackMD5 c = new CrackMD5("5d41402abc4b2a76b9719d911017c592");
			c.partitionStart = i * partitionSize;
			if ( i < tc - 1 )
				c.partitionEnd = (i+1) * partitionSize;
			else if (i == tc - 1)
				c.partitionEnd = codeArray.length;
			
			ths.add(c);
		}
		
		for (Thread t : ths)
			t.start();
		
		for (Thread t : ths)
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
	}
	
	
	public void run() {
		char[] raw = new char[testStrLength];
		testStringHelper(raw, 0, partitionStart, partitionEnd);
	}
	
	
	/**
	 * Construct the testing string recursively
	 * @param r		the constructing char[]
	 * @param k		current position of r
	 * @param start		the candidate char starting position in codeArray
	 * @param end		the candidate char ending position in codeArray
	 */
	private void testStringHelper(char[] r, int k, int start, int end) {
		if (k == r.length) {
			String str = new String(r);
			try {
				md = MessageDigest.getInstance("MD5");
				md.update(str.getBytes());
				md5 = bytesToHex(md.digest());
				if (md5.equals(goal)) {
					System.out.println(str);
				}
				int c = cntTest.incrementAndGet();
				if (c % 10_000_000 == 0)
					System.out.printf("#tested %d.\n", c);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = start; i < end; i++) {
				r[k] = codeArray[i];
				testStringHelper(r, k+1);
			}
		}
	}
	
	
	private void testStringHelper(char[] r, int k) {
		testStringHelper(r, k, 0, 62);
	}
	
	/**
	 * Convert byte arrays to Hex String
	 * @param input
	 * @return
	 */
	private String bytesToHex(byte[] input) {
		char[] hexarray = "0123456789abcdef".toCharArray();
		char[] hexChars = new char[input.length * 2];
		for (int i = 0; i < input.length; i++) {
			int v = input[i] & 0xff;
			hexChars[i*2] = hexarray[v>>4];
			hexChars[i*2+1] = hexarray[v & 0x0f];
		}
		return new String(hexChars);
	}
	
	
	public static void main(String[] argv) {
		crackString_mt(5, 4);
	}
}
