package com.hidef.fc.dedicated.admin;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface ServerRepository extends PagingAndSortingRepository<ServerConfig, String> {
}
