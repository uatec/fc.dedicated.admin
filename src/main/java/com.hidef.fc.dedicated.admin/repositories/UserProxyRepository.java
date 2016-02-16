package com.hidef.fc.dedicated.admin.repositories;

import com.hidef.fc.dedicated.admin.models.UserProxy;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface UserProxyRepository extends PagingAndSortingRepository<UserProxy, String> {
    UserProxy findByEmail(@Param(value = "email") String email);
}
