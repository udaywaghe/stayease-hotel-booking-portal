package com.stayease.assistant;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AttractionCatalog {
    private static final String MH="https://maharashtratourism.gov.in/districts/";
    private static final String DELHI="https://delhitourism.gov.in/dt/explore-the-city.html";
    private static final String GOA="https://goa-tourism.com/panoramic-view/";
    private static final String KARNATAKA="https://karnatakatourism.org/en/destinations/bengaluru/";
    private final List<AssistantResponse.Attraction> all = List.of(
        a("Mumbai","Gateway of India","Waterfront monument and well-known historic landmark.","Department of Tourism, Maharashtra",MH+"mumbai-city/"),
        a("Mumbai","Marine Drive","Coastal promenade known for sea views and evening walks.","Department of Tourism, Maharashtra",MH+"mumbai-city/"),
        a("Mumbai","Chhatrapati Shivaji Maharaj Terminus","Historic railway terminus recognized for its Victorian Gothic architecture.","Department of Tourism, Maharashtra",MH+"mumbai-city/"),
        a("Mumbai","Juhu Beach","Popular city beach and sunset spot.","Department of Tourism, Maharashtra",MH+"mumbai-suburban/"),
        a("Pune","Shaniwar Wada","Historic Peshwa-era fort and landmark.","Department of Tourism, Maharashtra",MH+"pune/"),
        a("Pune","Aga Khan Palace","Historic palace associated with India's freedom movement.","Department of Tourism, Maharashtra",MH+"pune/"),
        a("Pune","Sinhagad Fort","Hill fort and trekking destination.","Department of Tourism, Maharashtra",MH+"pune/"),
        a("Goa","Baga Beach","Beach known for its shoreline, water activities and nearby nightlife.","Goa Tourism Development Corporation",GOA),
        a("Goa","Fort Aguada","Historic fort on the Sinquerim peninsula.","Goa Tourism Development Corporation","https://goa-tourism.com/siteseeing/fort-aguada/"),
        a("Goa","Dudhsagar Falls","Waterfall listed among Goa's principal natural attractions.","Goa Tourism Development Corporation",GOA),
        a("Delhi","India Gate","National monument and central Delhi landmark.","Delhi Tourism",DELHI),
        a("Delhi","Humayun's Tomb","Historic garden tomb and heritage site.","Delhi Tourism",DELHI),
        a("Delhi","Qutub Minar","Historic minaret and monument.","Delhi Tourism",DELHI),
        a("Delhi","Lodhi Garden","Landscaped public garden with historic tombs.","Delhi Tourism",DELHI),
        a("Bengaluru","Lalbagh Botanical Garden","Historic botanical garden known for its Glass House and plant collections.","Karnataka Tourism",KARNATAKA),
        a("Bengaluru","Cubbon Park","Central city park and green retreat.","Karnataka Tourism",KARNATAKA),
        a("Bengaluru","Bangalore Palace","Royal residence with Tudor-style architecture.","Karnataka Tourism",KARNATAKA),
        a("Bengaluru","Bannerghatta Biological Park","Wildlife destination with zoo and safari experiences.","Karnataka Tourism",KARNATAKA));
    public List<AssistantResponse.Attraction> forCity(String city) {
        if (city == null) return List.of();
        return all.stream().filter(a -> a.sourceUrl().toLowerCase().contains(citySlug(city))).limit(4).toList();
    }
    private String citySlug(String city) {
        return switch(city.toLowerCase()) { case "mumbai" -> "mumbai"; case "pune" -> "pune"; case "goa" -> "goa"; case "delhi" -> "delhi"; case "bengaluru", "bangalore" -> "bengaluru"; default -> ""; };
    }
    private static AssistantResponse.Attraction a(String city,String name,String desc,String source,String url){return new AssistantResponse.Attraction(name,desc,source,url);}
}
