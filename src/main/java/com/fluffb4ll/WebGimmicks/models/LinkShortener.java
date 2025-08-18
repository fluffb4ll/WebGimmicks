package com.fluffb4ll.WebGimmicks.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDate;

@Entity
@EnableJpaRepositories
public class LinkShortener {
    @Column(nullable = false, unique = true, columnDefinition = "longtext")
    private String originalLink;

    @Id
    @Column(nullable = false, unique = true, columnDefinition = "char(5)")
    private String shortLink;

    @Column(columnDefinition = "date")
    private LocalDate lastUsed;

    @Column(nullable = false, columnDefinition = "date")
    private LocalDate createdOn;


    protected LinkShortener() {}

    public LinkShortener(String originalLink, String shortLink) {
        this.originalLink = originalLink;
        this.shortLink = shortLink;
        createdOn = LocalDate.now();
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


    public void changeLastUsed() {
        this.lastUsed =  LocalDate.now();
    }
}
