package com.y3technologies.masters.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.y3technologies.masters.model.ConfigCode;

@Repository
public interface ConfigCodeRepository extends BaseRepository<ConfigCode>, ConfigCodeRepositoryCustom {

    @Modifying
    @Query("update ConfigCode c set c.activeInd = :status WHERE c.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Boolean status);

    @Query("select c from ConfigCode c where c.activeInd = true and c.usageLevel in (:usageLevel , 'ALL')")
    List<ConfigCode> findByUsageLevel(@Param("usageLevel") String usageLevel);

    Optional<ConfigCode> findByCode(String code);

    int countByCode(@Param("code") String code);
}
