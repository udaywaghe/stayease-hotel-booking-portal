package com.stayease.assistant;

import com.stayease.dto.HotelResponse;
import com.stayease.dto.RoomResponse;
import com.stayease.service.HotelService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AssistantService {
    private final AiProvider ai;
    private final HotelService hotels;
    private final AttractionCatalog attractions;
    private final Map<String, Context> contexts = new ConcurrentHashMap<>();
    private static final Set<String> CITIES = Set.of("mumbai","pune","goa","delhi","bengaluru","bangalore");
    public AssistantService(AiProvider ai, HotelService hotels, AttractionCatalog attractions) { this.ai=ai;this.hotels=hotels;this.attractions=attractions; }

    public AssistantResponse chat(AssistantRequest request) {
        String sessionId = request.sessionId()==null || request.sessionId().isBlank() ? UUID.randomUUID().toString() : request.sessionId();
        if (!contexts.containsKey(sessionId) && contexts.size() >= 500) {
            java.time.Instant cutoff=java.time.Instant.now().minusSeconds(3600);
            contexts.entrySet().removeIf(entry->entry.getValue().touched.isBefore(cutoff));
            if(contexts.size()>=500) contexts.entrySet().stream().min(Comparator.comparing(entry->entry.getValue().touched))
                    .ifPresent(entry->contexts.remove(entry.getKey(),entry.getValue()));
        }
        Context context = contexts.computeIfAbsent(sessionId, ignored -> new Context());
        synchronized (context) {
            if(context.touched.isBefore(java.time.Instant.now().minusSeconds(3600))) context.clear();
            context.touched=java.time.Instant.now();
            if(context.lastUser!=null && context.location!=null) context.summary="location="+context.location+";budget="+context.budget+";checkIn="+context.checkIn+";checkOut="+context.checkOut+";guests="+context.guests+";amenities="+context.amenities+";days="+context.days+"; previous request="+context.lastUser;
            AssistantIntent intent = inheritContext(context,ai.extract(request.message(),context.summary));
            if("CHAT".equals(intent.intent())) {
                return response(sessionId,"I can help search StayEase hotels, compare room prices, find sourced city attractions, and plan a simple itinerary. Which city would you like to visit?","CHAT");
            }
            applyContext(context,intent,request.message());
            String city=context.location;
            if("ATTRACTIONS".equals(intent.intent()) || "ITINERARY".equals(intent.intent())) {
                if(city==null) return response(sessionId,"Which destination should I plan for? I currently have attraction guides for Mumbai, Pune, Goa, Delhi and Bengaluru.",intent.intent());
                List<AssistantResponse.Attraction> items=attractions.forCity(city);
                if("ITINERARY".equals(intent.intent())) {
                    int days=Math.max(1,Math.min(7,intent.days()==null?context.days==null?2:context.days:intent.days()));
                    List<AssistantResponse.ItineraryDay> plan=new ArrayList<>();
                    for(int d=1;d<=days;d++) { var selected=items.subList(Math.min((d-1)*2,items.size()),Math.min(d*2,items.size())); plan.add(new AssistantResponse.ItineraryDay(d,selected.isEmpty()?"Flexible day":"Explore "+city,selected.stream().map(AssistantResponse.Attraction::name).toList())); }
                    return new AssistantResponse(sessionId,"Here is a simple "+days+"-day outline using attractions in the official city guide. Check opening times and transport before you go; travel and entry costs are not included.","ITINERARY",List.of(),items,plan,"No attraction, transit or meal prices are estimated.",search(context,intent));
                }
                return new AssistantResponse(sessionId,items.isEmpty()?"I don't have verified attraction entries for that city yet.":"Here are attractions listed by the official tourism source. I’m omitting distances because the available data does not verify hotel-to-attraction distances.","ATTRACTIONS",List.of(),items,List.of(),null,search(context,intent));
            }
            if(city==null || !CITIES.contains(city.toLowerCase(Locale.ROOT))) return response(sessionId,"Which city should I search? I can search hotels in Mumbai, Pune, Goa, Delhi or Bengaluru.",intent.intent());
            if((intent.checkIn()==null)!=(intent.checkOut()==null)) return response(sessionId,"Please share both check-in and check-out dates if you want availability checked. You can also continue with price-only recommendations.",intent.intent());
            if(intent.checkIn()!=null && (intent.checkIn().isBefore(LocalDate.now()) || !intent.checkOut().isAfter(intent.checkIn()))) return response(sessionId,"Those dates are invalid. Please provide future check-in and a later check-out date.",intent.intent());
            if("CHEAPER".equals(intent.intent())) {
                if(context.budget==null) return response(sessionId,"What price per night would you like me to stay below?",intent.intent());
                context.budget=context.budget.multiply(new BigDecimal("0.80"));
            }
            List<HotelResponse> found=hotels.list(city,intent.checkIn(),intent.checkOut()).stream()
                    .map(h -> availableHotel(h,intent))
                    .filter(Objects::nonNull)
                    .filter(h->context.budget==null || h.rooms().stream().anyMatch(r->r.price().compareTo(context.budget)<=0))
                    .filter(h->intent.amenities().isEmpty() || intent.amenities().stream().allMatch(wanted->h.amenities().stream().anyMatch(a->a.equalsIgnoreCase(wanted))))
                    .filter(h->intent.guests()==null || h.rooms().stream().anyMatch(r->r.capacity()>=intent.guests()))
                    .sorted(Comparator.comparing(HotelResponse::price)).limit(6).toList();
            context.lastHotels=found;
            if(found.isEmpty()) return new AssistantResponse(sessionId,"I couldn't find matching hotel records in "+city+(context.budget==null?"":" under ₹"+context.budget.toPlainString())+". Try a higher nightly budget or remove an amenity filter.",intent.intent(),List.of(),List.of(),List.of(),null,search(context,intent));
            String prefix=intent.checkIn()==null?"These are matching hotels from the live StayEase database. Availability has not been checked because no dates were supplied.":"These hotels have matching room inventory for the requested dates based on confirmed bookings.";
            if("PRICE".equals(intent.intent())) prefix="These are actual per-night room prices from StayEase records. Accommodation totals below are estimates based on the selected dates and exclude other charges.";
            String est=null;
            if(intent.checkIn()!=null || ("PRICE".equals(intent.intent()) && context.days!=null)) {
                long nights=intent.checkIn()!=null?ChronoUnit.DAYS.between(intent.checkIn(),intent.checkOut()):context.days;
                BigDecimal min=found.stream().flatMap(h->h.rooms().stream()).map(RoomResponse::price).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                est="Lowest listed room estimate for "+nights+" night(s): ₹"+min.multiply(BigDecimal.valueOf(nights))+" before any additional charges. This is an estimate, not a booking or price guarantee.";
            }
            return new AssistantResponse(sessionId,prefix,"SEARCH".equals(intent.intent())?"SEARCH":intent.intent(),found,List.of(),List.of(),est,search(context,intent));
        }
    }
    private void applyContext(Context c,AssistantIntent i,String message) {
        if(i.location()!=null && CITIES.contains(i.location().toLowerCase(Locale.ROOT))) c.location=i.location().equalsIgnoreCase("bangalore")?"Bengaluru":i.location();
        if(i.budget()!=null && i.budget().signum()>0 && i.budget().compareTo(new BigDecimal("1000000"))<0)c.budget=i.budget();
        if(i.days()!=null && i.days()>0)c.days=i.days();
        if(i.checkIn()!=null && i.checkOut()!=null){c.checkIn=i.checkIn();c.checkOut=i.checkOut();}
        if(i.guests()!=null && i.guests()>0)c.guests=i.guests();
        if(!i.amenities().isEmpty())c.amenities=i.amenities();
        c.lastUser=message;
    }
    private AssistantIntent inheritContext(Context c,AssistantIntent i) {
        return new AssistantIntent(i.intent(),i.location(),i.budget(),i.checkIn()==null&&i.checkOut()==null?c.checkIn:i.checkIn(),
                i.checkIn()==null&&i.checkOut()==null?c.checkOut:i.checkOut(),i.guests()==null?c.guests:i.guests(),
                i.amenities().isEmpty()?c.amenities:i.amenities(),i.interests(),i.days()==null?c.days:i.days());
    }
    private HotelResponse availableHotel(HotelResponse hotel,AssistantIntent intent) {
        if(intent.checkIn()==null) return hotel;
        var availability=hotels.availability(hotel.id(),intent.checkIn(),intent.checkOut(),intent.guests()==null?1:intent.guests());
        if(!availability.available()) return null;
        List<RoomResponse> rooms=availability.rooms();
        BigDecimal price=rooms.stream().map(RoomResponse::price).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        return new HotelResponse(hotel.id(),hotel.name(),hotel.location(),hotel.area(),hotel.description(),hotel.image(),hotel.imageUrl(),hotel.rating(),hotel.reviews(),hotel.badge(),hotel.amenities(),price,rooms);
    }
    private AssistantResponse.SearchDetails search(Context c,AssistantIntent i){return new AssistantResponse.SearchDetails(c.location,c.budget,i.checkIn(),i.checkOut(),i.guests());}
    private AssistantResponse response(String id,String text,String intent){return new AssistantResponse(id,text,intent,List.of(),List.of(),List.of(),null,null);}
    private static class Context { String location,summary,lastUser; BigDecimal budget; Integer days,guests; LocalDate checkIn,checkOut; List<String> amenities=List.of(); List<HotelResponse> lastHotels=List.of(); java.time.Instant touched=java.time.Instant.now(); void clear(){location=null;summary=null;lastUser=null;budget=null;days=null;guests=null;checkIn=null;checkOut=null;amenities=List.of();lastHotels=List.of();} }
}
