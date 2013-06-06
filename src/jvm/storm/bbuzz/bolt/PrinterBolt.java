package storm.bbuzz.bolt;

import java.awt.image.BufferedImage;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;


public class PrinterBolt extends BaseBasicBolt {

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        Object obj = tuple.getValue(0);

        // write image height to proof we have good frame object
        System.out.println(String.valueOf(((BufferedImage)obj).getHeight()) + "x" + String.valueOf(((BufferedImage)obj).getWidth()));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd) {
    }
    
}
