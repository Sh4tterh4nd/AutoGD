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
	private Language serviceLanguage;

	@JsonProperty("campus_shortname")
	private String campusShortname;

	@JsonProperty("service_image")
	private String serviceImage;

	@JsonProperty("service_albumart")
	private String service_albumart;

	@JsonProperty("service_videolink")
	private String videoLink;

	private SeriesMetaData series;

	private PersonMetaData person;

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

	public PersonMetaData getPerson() {
		return person;
	}

	public void setPerson(PersonMetaData person) {
		this.person = person;
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

	public Language getServiceLanguage() {
		return serviceLanguage;
	}

	public void setServiceLanguage(Language serviceLanguage) {
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

	public String getServiceImage() {
		return serviceImage;
	}

	public void setServiceImage(String serviceImage) {
		this.serviceImage = serviceImage;
	}

	public String getService_albumart() {
		return service_albumart;
	}

	public void setService_albumart(String service_albumart) {
		this.service_albumart = service_albumart;
	}

	public String getVideoLink() {
		return videoLink;
	}

	public void setVideoLink(String videoLink) {
		this.videoLink = videoLink;
	}

	@Override
	public String toString() {
		return "WorshipMetaData{" +
				"serviceID=" + serviceID +
				", startDate=" + startDate +
				", startTime=" + startTime +
				", serviceLanguage=" + serviceLanguage +
				", campusShortname='" + campusShortname + '\'' +
				", serviceImage='" + serviceImage + '\'' +
				", service_albumart='" + service_albumart + '\'' +
				", series=" + series +
				", person=" + person +
				", serviceTitleByLanguage=" + serviceTitleByLanguage +
				'}';
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
