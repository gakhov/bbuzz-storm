package storm.bbuzz.utils;

import java.awt.image.BufferedImage;
import java.util.concurrent.LinkedBlockingQueue;

import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;


public class ImageSnapListener extends MediaListenerAdapter {
    private Double _frameInterval;

    // The video stream index, used to ensure we display frames from one and
    // only one video stream from the media container.
    private static int mVideoStreamIndex = -1;
    
    // Time of last frame write
    private static long mLastPtsWrite = Global.NO_PTS;
    
    protected LinkedBlockingQueue<BufferedImage> _queue;

    public ImageSnapListener(LinkedBlockingQueue<BufferedImage> queue, Double frameInterval){
        _queue = queue;
        _frameInterval = frameInterval;
    }

    private long getMicroSecondsBeetweenFrames(){
        return (long)(Global.DEFAULT_PTS_PER_SECOND * _frameInterval);
    }

    public void onVideoPicture(IVideoPictureEvent event) {
        if (event.getStreamIndex() != mVideoStreamIndex) {
            if (mVideoStreamIndex != -1){
                // no need to show frames from this video stream
                return;
            }

            // selected video stream id is not yet set
            // so let's select this lucky video stream
            mVideoStreamIndex = event.getStreamIndex();
        }

        long microsecondsInterval =  getMicroSecondsBeetweenFrames();
        // if uninitialized, back date mLastPtsWrite to get the very first frame
        if (mLastPtsWrite == Global.NO_PTS)
            mLastPtsWrite = event.getTimeStamp() - microsecondsInterval;

        // if it's time to write the next frame
        if (event.getTimeStamp() - mLastPtsWrite >= microsecondsInterval) {
            _queue.offer(event.getImage());
            // update last write time
            mLastPtsWrite += microsecondsInterval;
        }
    }
}