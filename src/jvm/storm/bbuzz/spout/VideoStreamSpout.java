package storm.bbuzz.spout;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import backtype.storm.Config;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.Global;

import storm.bbuzz.utils.ImageSnapListener;


public class VideoStreamSpout extends BaseRichSpout {
    SpoutOutputCollector _collector;
    LinkedBlockingQueue<BufferedImage> queue = null;
    IMediaReader _mediaReader;
    String _url;
    Double _frameInterval;
    
    public VideoStreamSpout(String url, Double frameInterval) {
        _url = url;
        _frameInterval = frameInterval;
    }
    
    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        queue = new LinkedBlockingQueue<BufferedImage>(1000);
        _collector = collector;
        _mediaReader = ToolFactory.makeReader(_url);
        _mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        _mediaReader.addListener(new ImageSnapListener(queue, _frameInterval));
        // read out the contents of the media file and
        // dispatch events to the attached listener
        while (_mediaReader.readPacket() == null) ;
    }

    @Override
    public void nextTuple() {
        BufferedImage ret = queue.poll();
        if(ret == null) {
            Utils.sleep(50);
        } else {
            _collector.emit(new Values(ret));
        }
    }

    @Override
    public void close() {
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config ret = new Config();
        ret.setMaxTaskParallelism(1);
        return ret;
    }    

    @Override
    public void ack(Object id) {
    }

    @Override
    public void fail(Object id) {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("frame"));
    }

}
