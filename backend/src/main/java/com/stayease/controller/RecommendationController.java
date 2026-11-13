package com.stayease.controller;
import com.stayease.dto.HotelResponse;
import com.stayease.service.RecommendationService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
@RestController @RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService service; public RecommendationController(RecommendationService service){this.service=service;}
    @GetMapping public List<HotelResponse> recommend(@RequestParam(required=false) String location,@RequestParam(required=false) BigDecimal budget,@RequestParam(required=false) String amenities){List<String> list=amenities==null||amenities.isBlank()?List.of():Arrays.stream(amenities.split(",")).map(String::trim).toList();return service.recommend(location,budget,list);}
}
