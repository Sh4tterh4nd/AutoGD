





### Full application.yml

Following is the application.yml with the default values.
```yaml
autogd:
  video:
    input-workspace: C:\Users\Arieh\Desktop\NewGD
    temp-workspace: ${autogd.video.input-workspace}\tmp
    resources:  ${autogd.video.input-workspace}\resources
    recordings: ${autogd.video.input-workspace}\recordings
    output: ${autogd.video.input-workspace}\video
    wav-target: ${autogd.video.input-workspace}\wav

    gdVideoStartTime: "00:22:53.5"
    gdVideoEndTime:  "00:25:20.5"

    precodec-param: ""
    codec: "h264_nvenc"

  youtube:
    title: |-
     [%SERIES_TITLE_LANGUAGE_[%SERVICE_LANGUAGE%]%]: [%SERVICE_TITLE_BY_LANGUAGE_[%SERVICE_LANGUAGE%]%]
    description: |-
      Gottesdienst vom [%START_DATE%] mit [%PERSON_FIRST_NAME%] [%PERSON_LAST_NAME%]
      Silbern Church, Dietikon

      Bleib mit uns in Verbindung:
      Kontaktformular: https://www.silbern.church/kontakt/#kontaktformular
      https://www.instagram.com/silbern.church
      https://www.facebook.com/silbern

      Als Kirche erleben wir heute noch, wie Gott Herzen berührt, Gebete beantwortet, Wunder tut und Menschen verändert.

      Mehr über die Silbern Church: https://www.silbern.church/
      Möchtest du die Silbern Church unterstützen? https://www.silbern.church/spenden/
    tags:
      - Gottesdienst
      - Church
      - Service
      - Silbern Church
    for-kids: false
    privacy-status: public
  gdverwaltung:
    select:
      location: "Silbern"
      date: 
      time: 
```


### Template Keys

- CAMPUS_SHORTNAME
- PERSON_ACTIVE
- PERSON_EMAIL
- PERSON_FACEBOOK
- PERSON_FIRST_NAME
- PERSON_ID
- PERSON_INSTAGRAM
- PERSON_LAST_NAME
- PERSON_PHONE
- PERSON_PICTURE
- PERSON_TWITTER
- PERSON_YOUTUBE
- SERIES_ALBUMART_LANGUAGE_ENGLISH
- SERIES_ALBUMART_LANGUAGE_GERMAN
- SERIES_ALBUMART_LANGUAGE_PORTUGUESE
- SERIES_ALBUMART_LANGUAGE_SPANISH
- SERIES_DESCRIPTION_LANGUAGE_ENGLISH
- SERIES_DESCRIPTION_LANGUAGE_GERMAN
- SERIES_DESCRIPTION_LANGUAGE_PORTUGUESE
- SERIES_DESCRIPTION_LANGUAGE_SPANISH
- SERIES_ID
- SERIES_IMAGE_LANGUAGE_ENGLISH
- SERIES_IMAGE_LANGUAGE_GERMAN
- SERIES_IMAGE_LANGUAGE_PORTUGUESE
- SERIES_IMAGE_LANGUAGE_SPANISH
- SERIES_SIGNAGE_LANGUAGE_ENGLISH
- SERIES_SIGNAGE_LANGUAGE_GERMAN
- SERIES_SIGNAGE_LANGUAGE_PORTUGUESE
- SERIES_SIGNAGE_LANGUAGE_SPANISH
- SERIES_TITLE_LANGUAGE_ENGLISH
- SERIES_TITLE_LANGUAGE_GERMAN
- SERIES_TITLE_LANGUAGE_PORTUGUESE
- SERIES_TITLE_LANGUAGE_SPANISH
- SERIES_URL_LANGUAGE_ENGLISH
- SERIES_URL_LANGUAGE_GERMAN
- SERIES_URL_LANGUAGE_PORTUGUESE
- SERIES_URL_LANGUAGE_SPANISH
- SERVICE_ID
- SERVICE_LANGUAGE
- SERVICE_TITLE_BY_LANGUAGE_ENGLISH
- SERVICE_TITLE_BY_LANGUAGE_GERMAN
- SERVICE_TITLE_BY_LANGUAGE_PORTUGUESE
- SERVICE_TITLE_BY_LANGUAGE_SPANISH
- START_DATE
- START_TIME