package com.dobebets.api.league;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "leagues", uniqueConstraints = @UniqueConstraint(name = "uk_league_name_country", columnNames = {"name", "country"}))
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 80)
    private String country;

    @Column(length = 10)
    private String code;

    @Column(nullable = false)
    private boolean active = true;
    @Column(name ="api_football_id", unique= true)
    private Long apiFootballId;

    protected League() {}

    public League(String name, String country, String code, boolean active) {
        this.name = name;
        this.country = country;
        this.code = code;
        this.active = active;
    }
    public void updateFromApiFootball(
        Long apiFootballId,
        String name,
        String country,
        String code
    ){
        this.apiFootballId = apiFootballId;
        this.name = name;
        this.country = country;
        this.code = code;
        this.active =true;

    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public String getCode() { return code; }
    public boolean isActive() { return active; }
    public Long getApiFootballId(){ return apiFootballId;}
}
