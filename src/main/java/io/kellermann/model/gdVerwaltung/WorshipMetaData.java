package io.kellermann.model.gdVerwaltung;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorshipMetaData implements Comparable<WorshipMetaData> {

	@JsonProperty("service_id")
	private Integer serviceID;

	@JsonProperty("service_startdate")
	private LocalDate startDate;

	@JsonProperty("service_starttime")
	private LocalTime startTime;

	@JsonProperty("service_language_id")
	private String serviceLanguage;

	@JsonProperty("campus_shortname")
	private String campusShortname;

	private SeriesMetaData series;

	private Map<Language, String> serviceTitleByLanguage = new HashMap<Language, String>();

	public String getServiceTitle(Language serviceLang) {
		return serviceTitleByLanguage.get(serviceLang);
	}

	public void addTitleLang(Language language, String value) {
		this.serviceTitleByLanguage.put(language, value);
	}

	public Integer getServiceID() {
		return serviceID;
	}

	public void setServiceID(Integer serviceID) {
		this.serviceID = serviceID;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public String getServiceLanguage() {
		return serviceLanguage;
	}

	public void setServiceLanguage(String serviceLanguage) {
		this.serviceLanguage = serviceLanguage;
	}

	public String getCampusShortname() {
		return campusShortname;
	}

	public void setCampusShortname(String theCampusShortname) {
		campusShortname = theCampusShortname;
	}

	public SeriesMetaData getSeries() {
		return series;
	}

	public void setSeries(SeriesMetaData series) {
		this.series = series;
	}

	public Map<Language, String> getServiceTitleByLanguage() {
		return serviceTitleByLanguage;
	}

	public void setServiceTitleByLanguage(Map<Language, String> serviceTitleByLanguage) {
		this.serviceTitleByLanguage = serviceTitleByLanguage;
	}

	@Override public String toString() {
		final StringBuilder sb = new StringBuilder("WorshipMetaData{");
		sb.append("serviceID=").append(serviceID);
		sb.append(", startDate=").append(startDate);
		sb.append(", startTime=").append(startTime);
		sb.append(", serviceLanguage='").append(serviceLanguage).append('\'');
		sb.append(", campusShortname='").append(campusShortname).append('\'');
		sb.append(", series=").append(series);
		sb.append(", serviceTitleByLanguage=").append(serviceTitleByLanguage);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public int compareTo(WorshipMetaData o) {
		if (!this.startDate.isEqual(o.startDate)){
			return this.startDate.compareTo(o.startDate);
		}else {
			return this.startTime.compareTo(o.startTime);
		}
	}
}
