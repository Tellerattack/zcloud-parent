package org.zonesion.hadoop.upload.view;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.zonesion.hadoop.base.util.LogListener;

public class UploadTool {
	
	private  int current = 0;
	
	private LogListener logListener;
	
	private Logger logger;

	public UploadTool() {
		super();
		//从类路径下加载配置文件
		PropertyConfigurator.configure(this.getClass().getClassLoader().getResourceAsStream("log4j/log4j.properties"));
		logger =  Logger.getLogger(UploadTool.class);
	}

	/**
	 * 拷贝文件夹
	 * 
	 * @param srcDir
	 * @param dstDir
	 * @param conf
	 * @return
	 * @throws Exception
	 */
	public  boolean copyDirectory(String srcDir, String dstDir,FileSystem fs) throws Exception {

		if (!fs.exists(new Path(dstDir))) {//目的路径是否存在，不存在则创建
			fs.mkdirs(new Path(dstDir));
		}
		FileStatus status = fs.getFileStatus(new Path(dstDir));
		File file = new File(srcDir);
		if (!status.isDir()) {
			System.exit(2);
			logger.info("You put in the " + dstDir + "is file !");
		} 
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				// 准备复制的源文件夹
				String srcDir1 = srcDir + File.separator + f.getName();
				String dstDir1 = dstDir + "/" + f.getName();//目标文件系统为linxu系统
				logger.info("src-dir:"+srcDir1);
				logger.info("dst-dir:"+dstDir1);
				copyDirectory(srcDir1, dstDir1, fs);
			} else {
				String dstfile = dstDir +"/"+f.getName() ;
				logger.info("src-file:"+f.getPath());
				logger.info("dst-file:"+dstfile);
				copyFile(f.getPath(),dstfile, fs);
				if(logListener != null) logListener.log("成功上传:"+f.getPath());
			}
		}
		return true;
	}

	/**
	 * 拷贝文件
	 * 
	 * @param src
	 * @param dst
	 * @param conf
	 * @return
	 * @throws Exception
	 */
	public  boolean copyFile(String src, String dst, FileSystem fs)
			throws Exception {
		current += 1;
		File file = new File(src);
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		//FieSystem的create方法可以为文件不存在的父目录进行创建，
		OutputStream out = fs.create(new Path(dst), new Progressable() {
			public void progress() {
			}
		});
		IOUtils.copyBytes(in, out, 4096, true);
		if(logListener != null) logListener.progress(current, UploadListener.Length);
		return true;
	}
	
	public void registerLogListener(LogListener logListener) {
		this.logListener = logListener;
	}
	
	public static void main(String[] args) throws Exception {
		UploadTool uploadTool = new UploadTool();
		if (args.length < 2) {
			//logger.info("Please input two number");
			System.exit(2);
		}
		String localSrc = args[0];
		String dst = args[1];
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		conf.set("fs.default.name", "hdfs://master.zonesion:9000");
		File srcFile = new File(localSrc);
		if (srcFile.isDirectory()) {
			uploadTool.copyDirectory(localSrc, dst, fs);
		} else {
			uploadTool.copyFile(localSrc, dst, fs);
		}
	}
}
