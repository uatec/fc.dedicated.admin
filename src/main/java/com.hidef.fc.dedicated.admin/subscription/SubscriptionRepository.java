package com.hidef.fc.dedicated.admin.subscription;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends PagingAndSortingRepository<Subscription, String> {
    List<Subscription> findSubscriptionByEmail(@Param(value = "email") String email);
}
