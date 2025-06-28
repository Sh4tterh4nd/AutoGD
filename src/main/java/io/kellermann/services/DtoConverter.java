package io.kellermann.services;

import io.kellermann.model.dto.WorshipMetaDataDTO;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DtoConverter {


    public WorshipMetaDataDTO worshipMetaDataToDto(WorshipMetaData worshipMetaData) {
        WorshipMetaDataDTO worshipMetaDataDTO = new WorshipMetaDataDTO();
        worshipMetaDataDTO.setServiceID(worshipMetaData.getServiceID());
        worshipMetaDataDTO.setMainLanguageTitle(worshipMetaData.getServiceTitle(worshipMetaData.getServiceLanguage()));
        worshipMetaDataDTO.setSeries(worshipMetaData.getSeries().getTitleLanguage(worshipMetaData.getServiceLanguage()));
        worshipMetaDataDTO.setDate(worshipMetaData.getStartDate());
        return worshipMetaDataDTO;
    }

    public List<WorshipMetaDataDTO> worshipMetaDataListToDto(List<WorshipMetaData> worshipMetaDataList) {
        return worshipMetaDataList.stream().map(this::worshipMetaDataToDto).toList();
    }
}
