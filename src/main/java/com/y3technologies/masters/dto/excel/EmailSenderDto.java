package com.y3technologies.masters.dto.excel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class EmailSenderDto
{

    private String templateName;

    private String templateType;

    private Map<String, String> parameters;

    private String from;

    private String fromName;

    private String to;

    private Map<String, String> attachmentsPath;
}
