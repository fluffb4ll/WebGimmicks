package com.fluffb4ll.WebGimmicks.controllers;

import com.fluffb4ll.WebGimmicks.models.LinkShortener;
import com.fluffb4ll.WebGimmicks.repositories.DNSRepo;
import com.fluffb4ll.WebGimmicks.repositories.LinkShortenerRepo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * Controller for Link Shortener webapp
 */
@RestController
public class LinkShortenerController {
    private final LinkShortenerRepo LSRepository;
    private final DNSRepo DNSRepository;

    public LinkShortenerController(LinkShortenerRepo LSRepository, DNSRepo DNSRepository) {
        this.LSRepository = LSRepository;
        this.DNSRepository = DNSRepository;
    }

    /**
     * Redirects user to the corresponding "original" link.
     * @param shortLink The short link
     * @return a {@link org.springframework.http.HttpStatus#FOUND FOUND (302)}
     *              redirect response if the short link is valid and exists in database;
     *         a {@link org.springframework.http.HttpStatus#BAD_REQUEST BAD REQUEST (400)}
     *              if the short link format is invalid;
     *         a {@link org.springframework.http.HttpStatus#NOT_FOUND NOT FOUND (404)}
     *              if the short link does not exist
     */
    @GetMapping("/ls/{shortLink}")
    public ResponseEntity<Void> redirectFromShortLink(@PathVariable String shortLink) {
        // check for invalid link
        if (!shortLink.matches("[A-Z]{5}")) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }
        LinkShortener linkShortener = LSRepository.findByShortLink(shortLink);

        // check for nonexistent link
        if (linkShortener == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        // transform unicode encoded link into ASCII string for redirect
        linkShortener.changeLastUsed();
        LSRepository.save(linkShortener);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(linkShortener.getOriginalLink()))
                .build();
    }

    /**
     * !SOON TO BE DEPRECATED! Check whether the specified domain name exists by querying a DNS provider.
     * @param domainName The domain name to check
     * @return {@code true} if domain name exists; {@code false} if it does not exist
     */
    private boolean verifyDomainName(String domainName) {
        String[] DNSEndpoints = DNSRepository.getAllEndpoints();
        for (String endpoint : DNSEndpoints) {
            try {
                java.net.URLConnection connection = new URI(endpoint + "?name=" + domainName + "&type=A").toURL().openConnection();
                connection.setRequestProperty("accept", "application/dns-json");
                HttpURLConnection http = (HttpURLConnection) connection;
                BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
                String content = in.readLine();
                in.close();
                http.disconnect();
                JsonObject dnsResponse = JsonParser.parseString(content).getAsJsonObject();

                if (dnsResponse.get("Status").getAsInt() == 0) {
                    return true;
                }
            } catch (IOException | URISyntaxException e) {
                System.err.println("[ERROR] URL verification failed. Endpoint: "
                        + endpoint + ". Error message: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Shortens given link by creating a corresponding short link in the database.
     * @param link the link to shorten
     * @return {@link org.springframework.http.HttpStatus#OK OK (200)}
     *              if the provided link already exists in the database;
     *         {@link org.springframework.http.HttpStatus#CREATED CREATED (201)}
     *              if the provided link was successfully added to the database;
     *         {@link org.springframework.http.HttpStatus#BAD_REQUEST BAD REQUEST (400)}
     *              if the provided link is invalid;
     *         {@link org.springframework.http.HttpStatus#INTERNAL_SERVER_ERROR INTERNAL SERVER ERROR (500)}
     *              if there are no free short links available for assignment;
     */
    @PostMapping("api/ls")
    public ResponseEntity<String> createShortLink(@RequestBody String link) {
        try {
            URI uri = URI.create(link);
            String scheme = uri.getScheme();
            if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
                throw new IllegalArgumentException("Unsupported URL scheme");
            }
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }

        // check if the link already exists
        LinkShortener linkShortener = LSRepository.findByOriginalLink(link);

        if (linkShortener != null) {
            return ResponseEntity.ok(linkShortener.getShortenedLink());
        }
        // create new short link
        else {
            char[] lastShortLink = LSRepository.findLastShortLink().toCharArray();

            for (int i = lastShortLink.length - 1; i >= 0; i--) {
                if (lastShortLink[i] != 'Z') {
                    lastShortLink[i]++;
                    break;
                }
                else if (lastShortLink[i] == 'Z' && i != 0) {
                    lastShortLink[i] = 'A';
                }
                // returns HTTP status 500 when short links end
                // TODO: a reminder to change this if i decide to fill the middle values
                else {
                    return ResponseEntity
                            .internalServerError()
                            .build();
                }
            }
            String shortLink = new String(lastShortLink);
            linkShortener = new LinkShortener(link, shortLink);
            try {
                LSRepository.save(linkShortener);
                return ResponseEntity.status(HttpStatus.CREATED).body(linkShortener.getShortenedLink());
            }
            catch (Exception e) {
                return ResponseEntity
                        .internalServerError()
                        .build();
            }

        }
    }
}
