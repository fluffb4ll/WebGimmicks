package com.fluffb4ll.WebGimmicks.models;

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Entity
@EnableJpaRepositories
public class DNS {
    @Id
    @Column(nullable = false, unique = true, columnDefinition = "int")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, columnDefinition = "varchar(128)")
    private String endpoint;


    protected DNS() {}

    public DNS(String endpoint) {
        this.endpoint = endpoint;
    }


    public int getId() {
        return id;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
