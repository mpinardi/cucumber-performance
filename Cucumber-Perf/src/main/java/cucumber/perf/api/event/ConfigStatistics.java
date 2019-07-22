package cucumber.perf.api.event;

public class ConfigStatistics extends TimeStampedEvent {

	public final String setting;
	
	public final Object value;
	
    public ConfigStatistics(Long timeStamp, long timeStampMillis,String setting,Object value) {
        super(timeStamp, timeStampMillis);
        this.setting = setting;
        this.value = value;
    }
}