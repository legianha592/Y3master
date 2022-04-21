package com.y3technologies.masters.repository;


import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.apiConfig.ApiConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiConfigRepository extends BaseRepository<ApiConfig>, ApiConfigRepositoryCustom {

    boolean existsByApiKey(String apiKey);

    boolean existsByLookupAndCustomer(Lookup lookup, Partners customer);

    boolean existsByUrnAndApiKey(String urn, String apiKey);

    boolean existsByUrnAndUsernameAndPassword(String urn, String username, String password);
}
