package com.y3technologies.masters.dto.aas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class SessionUserInfoDTO {

    private Long aasTenantId;
    private Long aasUserId;
    private Long aasTenantUserId;
    private String timezone;
    private List<SessionUserProfileDTO> profileDTOList = new ArrayList<>();

    public void addProfileDTO(Long refId, String profileCode, String profileValue) {
        Optional<SessionUserProfileDTO> findProfile = profileDTOList.stream().filter(pDTO -> profileCode.equals(pDTO.profileCode)).findFirst();
        findProfile.ifPresentOrElse(sessionUserProfileDTO -> sessionUserProfileDTO.addProfileInfo(new SessionUserProfileInfoDTO(refId, profileValue)),
                () -> {
                    SessionUserProfileDTO newProfile = new SessionUserProfileDTO(profileCode);
                    newProfile.addProfileInfo(new SessionUserProfileInfoDTO(refId, profileValue));
                    profileDTOList.add(newProfile);
                });
    }

    public void removeProfileDTO(SessionUserProfileDTO profileDTO) {
        this.profileDTOList.remove(profileDTO);
    }

    @Getter
    @Setter
    public static class SessionUserProfileDTO {
        String profileCode;
        List<SessionUserProfileInfoDTO> profileInfoList = new ArrayList<>();

        public SessionUserProfileDTO() { super(); }

        public SessionUserProfileDTO(String profileCode) {
            this.profileCode = profileCode;
        }

        public void addProfileInfo(SessionUserProfileInfoDTO profileInfoDTO) {
            this.profileInfoList.add(profileInfoDTO);
        }

        public void removeProfileInfo(SessionUserProfileInfoDTO profileInfoDTO) {
            this.profileInfoList.remove(profileInfoDTO);
        }
    }

    @Getter
    @Setter
    public static class SessionUserProfileInfoDTO {
        Long refId;
        String value;

        public SessionUserProfileInfoDTO() { super(); }

        public SessionUserProfileInfoDTO(Long refId, String value) {
            this.refId = refId;
            this.value = value;
        }
    }
}
