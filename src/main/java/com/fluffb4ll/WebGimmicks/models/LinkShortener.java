package com.fluffb4ll.WebGimmicks.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDate;

@Entity
@EnableJpaRepositories
public class LinkShortener {
    private String originalLink;

    @Id
    private String shortLink;

    private LocalDate lastUsed;

    private LocalDate createdOn;

    private boolean isActive;

    protected LinkShortener() {}

    public LinkShortener(String originalLink, String shortLink) {
        this.originalLink = originalLink;
        this.shortLink = shortLink;
    }

    public String getOriginalLink() {
        return originalLink;
    }

    public String getShortenedLink() {
        return shortLink;
    }

    public LocalDate getLastUsed() {
        return lastUsed;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void changeLastUsed() {
        this.lastUsed =  LocalDate.now();
    }
}
