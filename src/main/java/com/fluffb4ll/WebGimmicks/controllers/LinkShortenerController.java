package com.fluffb4ll.WebGimmicks.controllers;

import com.fluffb4ll.WebGimmicks.models.LinkShortener;
import com.fluffb4ll.WebGimmicks.repositories.LinkShortenerRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
public class LinkShortenerController {
    private final LinkShortenerRepo LSRepository;

    public LinkShortenerController(LinkShortenerRepo LSRepository) {
        this.LSRepository = LSRepository;
    }

    @GetMapping("/ls/{shortLink}")
    public String getShortLink(@PathVariable String shortLink, HttpServletResponse response, HttpServletRequest request) {
        LinkShortener linkShortener = LSRepository.findByShortLink(shortLink);
        if (linkShortener != null && linkShortener.getIsActive()) {
            String test = linkShortener.getOriginalLink();
            System.err.println(test);
            try {
                response.sendRedirect(linkShortener.getOriginalLink());
                linkShortener.changeLastUsed();
                LSRepository.save(linkShortener);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return "Awaiting redirect...";
        }
        else {
            return "Link is invalid";
        }
    }

    @PostMapping("api/ls")
    public ResponseEntity<String> createShortLink(@RequestBody String link) {
        LinkShortener linkShortener = LSRepository.findByOriginalLink(link);
        if (linkShortener != null) {
            return ResponseEntity.ok(linkShortener.getShortenedLink());
        } else {
            String shortLink = "";
            char[] lastShortLink = LSRepository.findLastShortLink().toCharArray();
            boolean reassignedChar = false;

            for (int i = lastShortLink.length - 1; i >= 0; i--) {
                if (lastShortLink[i] != 'Z' && !reassignedChar) {
                    shortLink += lastShortLink[i]++;
                    reassignedChar = true;
                }
                else {
                    shortLink += lastShortLink[i];
                }
            }
            shortLink = new StringBuilder(shortLink).reverse().toString();
            linkShortener = new LinkShortener(link, shortLink);
            try {
                LSRepository.save(linkShortener);
                return ResponseEntity.status(HttpStatus.CREATED).body(linkShortener.getShortenedLink());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

        }
    }
}
