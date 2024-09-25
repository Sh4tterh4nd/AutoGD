package io.kellermann.model.gdVerwaltung;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonMetaData {

    private Integer personId;
    private String personLastName;
    private String personFirstName;
    private String personPicture;
    private String personEmail;
    private String personPhone;
    private String personInstagram;
    private String personFacebook;
    private String personTwitter;
    private String personYoutube;
    private Boolean personActive;


    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public String getPersonLastName() {
        return personLastName;
    }

    public void setPersonLastName(String personLastName) {
        this.personLastName = personLastName;
    }

    public String getPersonFirstName() {
        return personFirstName;
    }

    public void setPersonFirstName(String personFirstName) {
        this.personFirstName = personFirstName;
    }

    public String getPersonPicture() {
        return personPicture;
    }

    public void setPersonPicture(String personPicture) {
        this.personPicture = personPicture;
    }

    public String getPersonEmail() {
        return personEmail;
    }

    public void setPersonEmail(String personEmail) {
        this.personEmail = personEmail;
    }

    public String getPersonPhone() {
        return personPhone;
    }

    public void setPersonPhone(String personPhone) {
        this.personPhone = personPhone;
    }

    public String getPersonInstagram() {
        return personInstagram;
    }

    public void setPersonInstagram(String personInstagram) {
        this.personInstagram = personInstagram;
    }

    public String getPersonFacebook() {
        return personFacebook;
    }

    public void setPersonFacebook(String personFacebook) {
        this.personFacebook = personFacebook;
    }

    public String getPersonTwitter() {
        return personTwitter;
    }

    public void setPersonTwitter(String personTwitter) {
        this.personTwitter = personTwitter;
    }

    public String getPersonYoutube() {
        return personYoutube;
    }

    public void setPersonYoutube(String personYoutube) {
        this.personYoutube = personYoutube;
    }

    public Boolean getPersonActive() {
        return personActive;
    }

    public void setPersonActive(Boolean personActive) {
        this.personActive = personActive;
    }
}
