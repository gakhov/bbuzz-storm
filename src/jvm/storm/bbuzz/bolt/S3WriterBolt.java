package storm.bbuzz.bolt;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.imageio.ImageIO;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;


public class S3WriterBolt extends BaseRichBolt {
    private OutputCollector _collector;

    private AmazonS3 _s3Client;
    private String _s3BucketName;
    private String _s3KeyPrefix;
        
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;

        BasicAWSCredentials awsCredentials = new BasicAWSCredentials((String) stormConf.get("S3_ACCESS_KEY"), (String) stormConf.get("S3_SECURE_KEY"));
        _s3Client = new AmazonS3Client(awsCredentials);

        _s3BucketName = (String) stormConf.get("S3_BACKET_NAME");
        if( !_s3Client.doesBucketExist(_s3BucketName) ){
            _s3Client.createBucket(_s3BucketName);
        }
        _s3KeyPrefix = (String) stormConf.get("S3_KEY_PREFIX");
    }

    @Override
    public void execute(Tuple tuple) {
        Object obj = tuple.getValue(0);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            ImageIO.write((BufferedImage)obj, "png", os);
        }catch(java.io.IOException e){
            System.out.println("Can't write buffered image");
            _collector.fail(tuple);
            return;
        }
        byte[] buffer = os.toByteArray();
        InputStream is = new ByteArrayInputStream(buffer);

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("image/png");
        meta.setContentLength(buffer.length);

        PutObjectResult result = _s3Client.putObject(new PutObjectRequest(_s3BucketName, getKeyName(), is, meta));
        String ETag = result.getETag();
        System.out.println("Etag: " + ETag);
        _collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
    
    private String getKeyName(){
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd.HH:mm:ss.SSS");
        return _s3KeyPrefix + "-" + dateFormatter.format(new Date()) + ".png";
    }
}
