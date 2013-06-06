package storm.bbuzz;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

import storm.bbuzz.bolt.GrayscaleBolt;
import storm.bbuzz.bolt.PrinterBolt;
import storm.bbuzz.bolt.S3WriterBolt;
import storm.bbuzz.spout.VideoStreamSpout;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;

class BbuzzTopology {

    public static Logger LOG = Logger.getLogger(BbuzzTopology.class);

    public static void main(String[] args) throws Exception {

        String topoName = args[0];
        String propertiesPath = args[1];
        
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesPath));

        // Configuration
        final Config conf = new Config();
        conf.setNumWorkers(Integer.parseInt(properties.getProperty("storm_num_workers")));
        conf.setMaxSpoutPending(Integer.parseInt(properties.getProperty("storm_max_spout_pending")));
        conf.setMaxTaskParallelism(Integer.parseInt(properties.getProperty("storm_max_task_parallel")));
        conf.setMessageTimeoutSecs(Integer.parseInt(properties.getProperty("storm_message_timeout_secs")));

        conf.put("VIDEO_STREAM_URL", properties.getProperty("video_stream_url"));
        conf.put("FRAME_INTERVAL_IN_SECONDS", Double.parseDouble(properties.getProperty("video_frame_interval_in_seconds")));

        conf.put("S3_ACCESS_KEY", properties.getProperty("s3_access_key"));
        conf.put("S3_SECURE_KEY", properties.getProperty("s3_secure_key"));
        conf.put("S3_BACKET_NAME", properties.getProperty("s3_bucket_name"));
        conf.put("S3_KEY_PREFIX", properties.getProperty("s3_key_prefix"));

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new VideoStreamSpout((String) conf.get("VIDEO_STREAM_URL"), (Double) conf.get("FRAME_INTERVAL_IN_SECONDS")), 1);
        builder.setBolt("grayscale", new GrayscaleBolt()).shuffleGrouping("spout");
        builder.setBolt("print", new PrinterBolt()).shuffleGrouping("grayscale");
        builder.setBolt("s3", new S3WriterBolt()).shuffleGrouping("grayscale");

        StormSubmitter.submitTopology(topoName, conf, builder.createTopology());
    }
}
