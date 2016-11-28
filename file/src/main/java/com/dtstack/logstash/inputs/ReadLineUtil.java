package com.dtstack.logstash.inputs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RandomAccessFile 方式按行读取文件 
 * @author xuchao
 *
 */
public class ReadLineUtil implements IReader{
	
	private static final Logger logger = LoggerFactory.getLogger(ReadLineUtil.class);
	
	private static byte lineBreak = '\n';
	private FileChannel channel;
	private String encoding;
	private long fileLength;
	private ByteBuffer buf;
	private long bufPos = 0;
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private RandomAccessFile raf;
	private int readSize = 0;
	private int skipNum = 0;
	private String fileName = "";
 
	public ReadLineUtil(File file, String encoding, int pos)
			throws IOException {
		this.fileName = file.getPath();
		raf = new RandomAccessFile(file, "r");
		init(encoding, pos);
	}
	
	public ReadLineUtil(File file, String encoding, String startPos) throws IOException{
		raf = new RandomAccessFile(file, "r");
		this.fileName = file.getPath();
		if("beginning".equalsIgnoreCase(startPos)){
			init(encoding, 0);
		}else{
			init(encoding, (int)raf.length());
		}
	}
	
	private void init(String encoding, int pos) throws IOException{
		channel = raf.getChannel();
		fileLength = raf.length();
		this.encoding = encoding;
		this.skipNum = pos;
		readSize = (int) (fileLength - this.skipNum);
		buf = channel.map(FileChannel.MapMode.READ_ONLY, this.skipNum, this.readSize);
	}
	
	@Override
	public String readLine() throws UnsupportedEncodingException{
		
		while (bufPos < readSize) {
			byte c = buf.get((int)bufPos);//FIXME if the file size max then Integer.Max,it is err
			bufPos++;
			if (c == '\r' || c == lineBreak) {
				if (c != lineBreak) {
					continue;
				}
				return bufToString();
			}
			baos.write(c);
		}
		
		if(baos.size() != 0){
			return bufToString();
		}
		
		doAfterReaderOver();
		return null;
		
	}
	
	@Override
	public void doAfterReaderOver(){
		try {
			channel.close();
			raf.close();
		} catch (IOException e) {
			logger.error("", e);
		}
	}
	
	private String bufToString() throws UnsupportedEncodingException {
		if (baos.size() == 0) {
			return "";
		}
 
		byte[] bytes = baos.toByteArray();
 
		baos.reset();
		return new String(bytes, encoding);
	}
	
	public static void setDelimiter(byte lineBreak){
		ReadLineUtil.lineBreak = lineBreak;
	}
	
	@Override
	public int getCurrBufPos(){
		return (int) (skipNum + bufPos);
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public boolean needMonitorChg() {
		return true;
	}
}