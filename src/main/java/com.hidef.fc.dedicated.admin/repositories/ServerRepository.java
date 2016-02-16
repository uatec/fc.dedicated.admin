package com.hidef.fc.dedicated.admin.repositories;

import com.hidef.fc.dedicated.admin.models.ServerConfig;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface ServerRepository extends PagingAndSortingRepository<ServerConfig, String> {
    ServerConfig findByImplementationId(@Param(value = "implementationId") String implementationId);
}
