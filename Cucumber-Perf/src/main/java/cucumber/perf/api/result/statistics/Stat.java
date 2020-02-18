package cucumber.perf.api.result.statistics;

import static java.util.Locale.ROOT;

public class Stat {
	private String key;
	private String fullName;
	private String shortName;
	private String abbrivation;
	private StatDataType dataType;
	private static String SEP = "_";
	
	public Stat (String prefix, Stat stat)
	{
		this.key = prefix+SEP+stat.getKey();
		this.fullName = prefix+SEP+stat.getFullName();
		this.dataType = StatDataType.fromLowerCaseName(stat.getDataType().lowerCaseName());
		this.abbrivation = prefix+SEP+stat.getAbbrivation();
		this.shortName = prefix+SEP+stat.getShortName();
	}
	
	public Stat (Stat stat, String postfix)
	{
		this.key = stat.getKey()+SEP+postfix;
		this.fullName = stat.getFullName()+SEP+postfix;
		this.dataType = StatDataType.fromLowerCaseName(stat.getDataType().lowerCaseName());
		this.abbrivation = stat.getAbbrivation()+SEP+postfix;
		this.shortName = stat.getShortName()+SEP+postfix;
	}
	
	public Stat (String key, String fullName, String shortName,String abbrivation, StatDataType dataType)
	{
		this.key = key;
		this.fullName = fullName;
		this.dataType = dataType;
		this.abbrivation = abbrivation;
		this.shortName = shortName;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public StatDataType getDataType() {
		return dataType;
	}

	public void setType(StatDataType dataType) {
		this.dataType = dataType;
	}

	public String getAbbrivation() {
		return abbrivation;
	}

	public void setAbbrivation(String abbrivation) {
		this.abbrivation = abbrivation;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public enum StatDataType {
		COUNT,
		OTHER,
		NANOS,
        MILLIS,
        SECONDS;

        public static StatDataType fromLowerCaseName(String lowerCaseName) {
            return valueOf(lowerCaseName.toUpperCase(ROOT));
        }

        public String lowerCaseName() {
            return name().toLowerCase(ROOT);
        }

        public String firstLetterCapitalizedName() {
            return name().substring(0, 1) + name().substring(1).toLowerCase(ROOT);
        }
    }
}
