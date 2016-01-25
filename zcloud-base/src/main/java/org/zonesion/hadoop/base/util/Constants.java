package org.zonesion.hadoop.base.util;

import java.io.File;

public class Constants {
	public static final String HOMEPATH = System.getProperty("user.home");
	public static final String PROJECTPARENT = "zcloud-parent";
	public static final String LOCAL_FILE = "local_update.properties";
	public static final String HDFS_FILE = "hdfs_update.properties";
	public static final String MKDIRPATH = HOMEPATH+File.separator+PROJECTPARENT;
	public static final String LOCAL_PATH = MKDIRPATH+File.separator+LOCAL_FILE;//类路径配置文件：记录下载最后更新的时间点
	public static final String HDFS_PATH =  MKDIRPATH+File.separator+HDFS_FILE;//类路径配置文件：记录下载最后更新的时间点
	public static final String JAR_HOME = MKDIRPATH + File.separator + "zcloud-mapreduce-1.0-SNAPSHOT.jar";

}
