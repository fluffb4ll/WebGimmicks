package com.fluffb4ll.WebGimmicks.repositories;

import com.fluffb4ll.WebGimmicks.models.LinkShortener;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "ls", path = "ls")
public interface LinkShortenerRepo extends CrudRepository<LinkShortener, String> {
    LinkShortener findByShortLink(String shortenedLink);

    LinkShortener findByOriginalLink(String originalLink);

    List<LinkShortener> findByIsActive(Boolean isActive);
}
