package com.stayease.service;

import com.stayease.dto.HotelResponse;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class RecommendationService {
    private final HotelService hotelService;
    public RecommendationService(HotelService hotelService){this.hotelService=hotelService;}
    public List<HotelResponse> recommend(String location,BigDecimal budget,List<String> requestedAmenities){LocalDate in=LocalDate.now().plusDays(1),out=in.plusDays(1);List<HotelResponse> stays=hotelService.list(null,in,out);List<String> amenities=requestedAmenities==null?List.of():requestedAmenities.stream().filter(Objects::nonNull).map(String::trim).filter(s->!s.isBlank()).toList();return stays.stream().filter(h->budget==null||h.price().compareTo(budget)<=0).sorted(Comparator.<HotelResponse>comparingInt(h->score(h,location,budget,amenities)).reversed().thenComparing(HotelResponse::rating,Comparator.reverseOrder())).limit(6).toList();}
    private int score(HotelResponse h,String location,BigDecimal budget,List<String> amenities){int score=1;if(location!=null&&!location.isBlank()){String q=location.toLowerCase(Locale.ROOT);if(h.location().toLowerCase(Locale.ROOT).contains(q)||h.area().toLowerCase(Locale.ROOT).contains(q))score+=5;}if(budget!=null&&h.price().compareTo(budget)<=0)score+=2;for(String a:amenities)if(h.amenities().stream().anyMatch(x->x.equalsIgnoreCase(a)))score++;return score;}
}
