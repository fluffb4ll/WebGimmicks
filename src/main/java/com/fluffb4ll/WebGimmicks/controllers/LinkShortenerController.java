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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;


@RestController
public class LinkShortenerController {
    private final LinkShortenerRepo LSRepository;
    private final DNSRepo DNSRepository;
    private final HashMap<Character, char[]> percentEncodingDict = new HashMap<>(Map.ofEntries(
            entry(' ', new char[] {'%', '2', '0'}),
            entry('@', new char[] {'%', '4', '0'}),
            entry('!', new char[] {'%', '2', '1'}),
            entry('#', new char[] {'%', '2', '3'}),
            entry('$', new char[] {'%', '2', '4'}),
            // this one causes problems if provided an already percent-encoded link
            //entry('%', new char[] {'%', '2', '5'}),
            entry('&', new char[] {'%', '2', '6'}),
            entry('\'', new char[] {'%', '2', '7'}),
            entry('(', new char[] {'%', '2', '8'}),
            entry(')', new char[] {'%', '2', '9'}),
            entry('*', new char[] {'%', '2', 'A'}),
            entry('+', new char[] {'%', '2', 'B'}),
            entry(',', new char[] {'%', '2', 'C'}),
            // why tf was it included in mozilla docs...
            //entry('/', new char[] {'%', '2', 'F'}),
            entry(':', new char[] {'%', '3', 'A'}),
            entry('[', new char[] {'%', '5', 'B'}),
            entry(';', new char[] {'%', '3', 'B'}),
            entry(']', new char[] {'%', '5', 'D'}),
            entry('=', new char[] {'%', '3', 'D'}),
            entry('?', new char[] {'%', '3', 'F'})));

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

    // TODO
    private String linkToPunycode(String link) {
        return "test";
    }

    private String percentEncode(String link) {
        int arrayPtr = 0;
        char[] encodedLink = new char[link.length()];
        for (char c : link.toCharArray()) {
            if (percentEncodingDict.containsKey(c)) {
                encodedLink = Arrays.copyOf(encodedLink, encodedLink.length + 2);
                for (char d : percentEncodingDict.get(c)) {
                    encodedLink[arrayPtr] += d;
                    arrayPtr++;
                }
            }
            else {
                encodedLink[arrayPtr] = c;
                arrayPtr++;
            }
        }
        return new String(encodedLink);
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
        link = link.toLowerCase();
        String[] linkParts = link.split("/");
        link = linkToPunycode(linkParts[0]) + "/" + percentEncode(String.join("/", Arrays.copyOfRange(linkParts, 1, linkParts.length)));
        String allowedSymbols = "-_~";
        if (!link.matches(String.format("\\w.(\\w+[%s]*)+", allowedSymbols)) || !verifyDomainName(link.split("/")[0])) {
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
