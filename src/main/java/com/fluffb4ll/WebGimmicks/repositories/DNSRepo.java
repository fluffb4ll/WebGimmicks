package com.fluffb4ll.WebGimmicks.repositories;

import com.fluffb4ll.WebGimmicks.models.DNS;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DNSRepo extends CrudRepository<DNS, String> {
    @Query("select distinct dns.endpoint from DNS dns")
    String[] getAllEndpoints();
}
