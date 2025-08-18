package com.fluffb4ll.WebGimmicks.controllers;

import com.fluffb4ll.WebGimmicks.models.LinkShortener;
import com.fluffb4ll.WebGimmicks.repositories.DNSRepo;
import com.fluffb4ll.WebGimmicks.repositories.LinkShortenerRepo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletResponse;
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


@RestController
public class LinkShortenerController {
    private final LinkShortenerRepo LSRepository;
    private final DNSRepo DNSRepository;

    public LinkShortenerController(LinkShortenerRepo LSRepository, DNSRepo DNSRepository) {
        this.LSRepository = LSRepository;
        this.DNSRepository = DNSRepository;
    }

    @GetMapping("/ls/{shortLink}")
    public String redirectFromShortLink(@PathVariable String shortLink, HttpServletResponse response) {
        LinkShortener linkShortener = LSRepository.findByShortLink(shortLink);
        if (linkShortener != null) {
            try {
                response.sendRedirect("https://" + linkShortener.getOriginalLink());
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
                System.err.println("[ERROR] URL verification failed. Endpoint: " + endpoint + ". Error message: " + e.getMessage());
            }
        }
        return false;
    }

    // TODO: добавить больше букавок
    @PostMapping("api/ls")
    public ResponseEntity<String> createShortLink(@RequestBody String link) {
        if (!verifyDomainName(link.split("/")[0])) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad domain name!");
        }

        LinkShortener linkShortener = LSRepository.findByOriginalLink(link);

        if (linkShortener != null) {
            return ResponseEntity.ok(linkShortener.getShortenedLink());
        } else {
            char[] lastShortLink = LSRepository.findLastShortLink().toCharArray();
            boolean reassignedChar = false;

            for (int i = lastShortLink.length - 1; i >= 0; i--) {
                if (lastShortLink[i] != 'Z' && !reassignedChar) {
                    lastShortLink[i]++;
                    break;
                }
                else if (lastShortLink[i] == 'Z' && !reassignedChar) {
                    lastShortLink[i] = 'A';
                }
            }
            String shortLink = new String(lastShortLink);
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
