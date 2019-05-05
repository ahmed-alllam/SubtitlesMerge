import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.fredy.jsrt.api.SRT;
import org.fredy.jsrt.api.SRTInfo;
import org.fredy.jsrt.api.SRTReader;
import org.fredy.jsrt.api.SRTTimeFormat;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws FrameRecorder.Exception, FrameGrabber.Exception {
        long start=System.nanoTime();
        Java2DFrameConverter converter=new Java2DFrameConverter();
        Scanner reader=new Scanner(System.in);
        System.out.println("Please Enter SRT file Path");
        SRTInfo info = SRTReader.read(new File(reader.nextLine()));
        System.out.println("Please Enter Video file Path");
        String video_file=reader.nextLine();
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(video_file);
        frameGrabber.start();
        FFmpegFrameRecorder recorder = FFmpegFrameRecorder.createDefault(video_file.substring(0,video_file.lastIndexOf("."))+"subtitled."+frameGrabber.getFormat(), frameGrabber.getImageWidth(), frameGrabber.getImageHeight());
        recorder.setFrameRate(frameGrabber.getFrameRate());
        recorder.setVideoQuality(1);
        recorder.setAudioChannels(frameGrabber.getAudioChannels());
        recorder.setSampleRate(frameGrabber.getSampleRate());
        recorder.start();
        for (int i = 1; i <= Double.MAX_VALUE;i++){
            Frame frame=frameGrabber.grabFrame();
            int z=0;
            if (frame!=null) {
                if (frame.image != null) {
                    for (SRT s : info) {
                        if (parse(s)[0] * frameGrabber.getFrameRate() <= i && parse(s)[1] * frameGrabber.getFrameRate() >= i) {
                            BufferedImage bf = converter.convert(frame);
                            Graphics g = bf.createGraphics();
                            StringBuilder t = new StringBuilder();
                            for (String line : s.text) {
                                t.append(line);
                            }
                            g.drawString(t.toString(), bf.getWidth() / 2, bf.getHeight() / 2);
                            recorder.record(converter.convert(bf));
                            break;
                        } else {
                            z++;
                            if (z == info.size()) {
                                recorder.record(frame);
                            }
                        }
                    }
                } else {
                    recorder.record(frame);
                }
            }else {
                break;
            }
        }
        frameGrabber.stop();
        recorder.stop();
        System.out.println(System.nanoTime()-start);
    }
    private static double [] parse(SRT s){

        String [] chararray=SRTTimeFormat.format(s.startTime).split(":");

        String [] sec1 =chararray[2].split(",");
        String sec=sec1[0];
        String millisec=sec1[1];

        String minuets =chararray[1];

        String hours =chararray[0];

        double i=(Double.parseDouble(hours)*3600)+(Double.parseDouble(minuets)*60)+(Double.parseDouble(sec))+(Double.parseDouble(millisec)/1000);


        String [] chararray2=SRTTimeFormat.format(s.endTime).split(":");

        String [] sec2 =chararray2[2].split(",");
        String sec3=sec2[0];
        String millisec1=sec2[1];

        String minuets1 =chararray2[1];

        String hours1 =chararray2[0];

        double i2=(Double.parseDouble(hours1)*3600)+(Double.parseDouble(minuets1)*60)+(Double.parseDouble(sec3))+(Double.parseDouble(millisec1)/1000);

        return new double[]{i,i2};
    }
}