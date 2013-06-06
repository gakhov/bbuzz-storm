package storm.bbuzz.bolt;

import java.awt.color.ColorSpace;
import java.awt.image.ColorConvertOp;
import java.awt.image.BufferedImage;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;


public class GrayscaleBolt extends BaseRichBolt {
    private OutputCollector _collector;
        
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        Object obj = tuple.getValue(0);

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);

        _collector.emit(tuple, new Values(op.filter((BufferedImage)obj, null)));
        _collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("frame"));
    }
    
}
