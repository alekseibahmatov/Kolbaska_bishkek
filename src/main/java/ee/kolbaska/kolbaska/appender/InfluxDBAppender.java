package ee.kolbaska.kolbaska.appender;

import java.time.Instant;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfluxDBAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private String url;
    private String token;
    private String org;
    private String bucket;
    private InfluxDBClient influxDBClient;

    @Override
    public void start() {
        influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray());
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        try (WriteApi writeApi = influxDBClient.makeWriteApi()) {
            Point point = Point.measurement("logEvent")
                    .time(Instant.ofEpochMilli(event.getTimeStamp()), WritePrecision.MS)
                    .addField("level", event.getLevel().toString())
                    .addField("message", event.getFormattedMessage())
                    .addField("logger", event.getLoggerName())
                    .addField("thread", event.getThreadName());

            writeApi.writePoint(bucket, org, point);
        }
    }

    @Override
    public void stop() {
        if (influxDBClient != null) {
            influxDBClient.close();
        }
        super.stop();
    }

}
