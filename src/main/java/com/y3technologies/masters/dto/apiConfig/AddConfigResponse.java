package com.y3technologies.masters.dto.apiConfig;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AddConfigResponse {
    private Integer status;
    private String message;
}
