package com.hidef.fc.dedicated.admin.server;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Greg Turnquist
 */
// tag::code[]
public interface ServerRepository extends PagingAndSortingRepository<Server, String> {

}

