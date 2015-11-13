package org.zonesion.hadoop.mr.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.zonesion.hadoop.base.util.PropertiesHelper;

public class Main {

	// 在MapReduce中，由Job对象负责管理和运行一个计算任务，并通过Job的一些方法对任>务的参数进行相关的设置。
	public static void main(String[] args) throws Exception {
		String tablename = "zcloud";
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum",PropertiesHelper.getInstance("hbase-config.properties").getProperty("hbase.zookeeper.quorum"));
		conf.addResource("classpath:/hadoop/core-site.xml");
		conf.addResource("classpath:/hadoop/hdfs-site.xml");
		conf.addResource("classpath:/hadoop/mapred-site.xml");
		Job job = new Job(conf, "PMHBaseMain");
		job.setJarByClass(Main.class);
		job.setMapperClass(PMMapper.class);
		TableMapReduceUtil.initTableReducerJob(tablename, PMReduce.class, job);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		/**** 准备输出 *****/
		prepareOutput(conf, tablename);
		/**** 准备输入 *****/
		FileSystem fs = FileSystem.get(conf);
		for (String str : listFile(fs, "/user/hadoop/zcloud")) {// 遍历/user/hadoop/zcloud下的输入
			for (String str1 : listFile(fs, str)) {
				FileInputFormat.addInputPath(job, new Path(str1));
			}
		}
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

	public static void prepareOutput(Configuration conf, String tablename) {
		HBaseAdmin admin = null;
		try {
			admin = new HBaseAdmin(conf);
			if (!admin.tableExists(tablename)) {
				System.out.println("table not exists!creating.......");
				HTableDescriptor htd = new HTableDescriptor(tablename);
				HColumnDescriptor tcd = new HColumnDescriptor("content");
				htd.addFamily(tcd);// 创建列族
				admin.createTable(htd);// 创建表
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (admin != null)
				try {
					admin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public static List<String> listFile(FileSystem fs, String filePath) throws IOException {
		List<String> listDir = new ArrayList<String>();
		Path path = new Path(filePath);
		FileStatus[] fileStatus = fs.listStatus(path);
		for (FileStatus file : fileStatus) {
			listDir.add(file.getPath().toString());
		}
		return listDir;
	}
}
