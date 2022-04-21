package com.y3technologies.masters.dto.comm;

import lombok.Data;

import java.io.Serializable;

/**
 * @author beekhon.ong
 */
@Data
public class CityDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String cityFullName;

    private boolean isActive;

}
