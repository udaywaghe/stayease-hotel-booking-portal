package com.stayease.assistant;

import com.stayease.dto.HotelResponse;
import com.stayease.dto.RoomResponse;
import com.stayease.dto.AvailabilityResponse;
import com.stayease.service.HotelService;
import com.stayease.exception.GlobalExceptionHandler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class AssistantServiceTest {
    @Mock AiProvider ai;
    @Mock HotelService hotels;
    AssistantService service;

    @BeforeEach void setup() { service=new AssistantService(ai,hotels,new AttractionCatalog()); }

    @Test void asksForDestinationWhenSearchHasNoLocation() {
        when(ai.extract("find hotels",null)).thenReturn(new AssistantIntent("SEARCH",null,null,null,null,null,List.of(),null,null));
        var response=service.chat(new AssistantRequest("find hotels",null));
        assertTrue(response.reply().contains("Which city"));
        assertTrue(response.hotels().isEmpty());
        verifyNoInteractions(hotels);
    }

    @Test void returnsOnlyDatabaseHotelsWithinBudget() {
        when(ai.extract("Find hotels in Mumbai below ₹5,000",null)).thenReturn(new AssistantIntent("SEARCH","Mumbai",new BigDecimal("5000"),null,null,null,List.of(),null,null));
        HotelResponse cheap=hotel("hCheap","Budget Mumbai",4200);
        HotelResponse expensive=hotel("hExp","Premium Mumbai",12000);
        when(hotels.list("Mumbai",null,null)).thenReturn(List.of(cheap,expensive));
        var response=service.chat(new AssistantRequest("Find hotels in Mumbai below ₹5,000",null));
        assertEquals(List.of("hCheap"),response.hotels().stream().map(HotelResponse::id).toList());
        assertTrue(response.reply().contains("live StayEase database"));
    }

    @Test void followupCheaperRetainsCityAndReducesPreviousBudget() {
        when(ai.extract("Find hotels in Goa below 10000",null)).thenReturn(new AssistantIntent("SEARCH","Goa",new BigDecimal("10000"),null,null,null,List.of(),null,null));
        when(ai.extract(eq("show me cheaper"), anyString()))
                .thenReturn(new AssistantIntent("CHEAPER",null,null,null,null,null,List.of(),null,null));
        HotelResponse cheaper=hotel("hGoa","Goa Beach Hotel",7000);
        when(hotels.list("Goa",null,null)).thenReturn(List.of(cheaper));
        var first=service.chat(new AssistantRequest("Find hotels in Goa below 10000",null));
        var response=service.chat(new AssistantRequest("show me cheaper",first.sessionId()));
        assertEquals(new BigDecimal("8000.00"),response.search().budget().setScale(2));
        assertEquals("hGoa",response.hotels().getFirst().id());
    }

    @Test void attractionResponseUsesOfficialSourceAndOmitsUnverifiedDistance() {
        when(ai.extract("what can I explore in Pune",null)).thenReturn(new AssistantIntent("ATTRACTIONS","Pune",null,null,null,null,List.of(),null,null));
        var response=service.chat(new AssistantRequest("what can I explore in Pune",null));
        assertFalse(response.attractions().isEmpty());
        assertTrue(response.attractions().stream().allMatch(a->a.sourceUrl().startsWith("https://")));
        assertTrue(response.reply().contains("omitting distances"));
    }

    @Test void rejectsPartialDateRangeWithFollowup() {
        when(ai.extract("find Goa hotels for 2027-02-10",null)).thenReturn(new AssistantIntent("SEARCH","Goa",null,java.time.LocalDate.parse("2027-02-10"),null,null,List.of(),null,null));
        var response=service.chat(new AssistantRequest("find Goa hotels for 2027-02-10",null));
        assertTrue(response.reply().contains("both check-in and check-out"));
        verifyNoInteractions(hotels);
    }

    @Test void providerFailureIsPropagatedForControllerToReturnUnavailable() {
        AiProviderException failure=new AiProviderException("provider down",new RuntimeException());
        when(ai.extract("find hotels",null)).thenThrow(failure);
        assertSame(failure,assertThrows(AiProviderException.class,()->service.chat(new AssistantRequest("find hotels",null))));
        var response=new GlobalExceptionHandler().handleAiUnavailable(failure,new MockHttpServletRequest("POST","/api/assistant/chat"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,response.getStatusCode());
    }

    @Test void localExtractorUnderstandsRupeeBudgetAndTripDuration() {
        RuleBasedAiProvider local=new RuleBasedAiProvider();
        var search=local.extract("Find hotels in Mumbai below "+'\u20B9'+"5,000",null);
        var trip=local.extract("Plan a 3 day Goa trip",null);
        assertEquals(new BigDecimal("5000"),search.budget());
        assertEquals("Mumbai",search.location());
        assertEquals("ITINERARY",trip.intent());
        assertEquals(3,trip.days());
    }

    @Test void cheaperFollowupRetainsDateAndGuestAvailabilityFilters() {
        var in=java.time.LocalDate.parse("2027-12-10"); var out=java.time.LocalDate.parse("2027-12-12");
        when(ai.extract("Find Goa hotels below 10000 for 2 guests",null)).thenReturn(new AssistantIntent("SEARCH","Goa",new BigDecimal("10000"),in,out,2,List.of(),null,null));
        when(ai.extract(eq("show me cheaper"),anyString())).thenReturn(new AssistantIntent("CHEAPER",null,null,null,null,null,List.of(),null,null));
        var hotel=hotel("hGoa","Goa Beach Hotel",7000); var room=hotel.rooms().getFirst();
        when(hotels.list("Goa",in,out)).thenReturn(List.of(hotel));
        when(hotels.availability("hGoa",in,out,2)).thenReturn(new AvailabilityResponse("hGoa",in,out,2,true,List.of(room)));
        var first=service.chat(new AssistantRequest("Find Goa hotels below 10000 for 2 guests",null));
        var response=service.chat(new AssistantRequest("show me cheaper",first.sessionId()));
        assertEquals(in,response.search().checkIn()); assertEquals(out,response.search().checkOut());
        assertEquals(2,response.search().guests());
        verify(hotels,times(2)).availability("hGoa",in,out,2);
    }

    private HotelResponse hotel(String id,String name,int price) {
        return new HotelResponse(id,name,"Mumbai","Area","Description","https://example.com/image.jpg","https://example.com/image.jpg",4.5,10,"Stay",List.of("Pool"),BigDecimal.valueOf(price),List.of(new RoomResponse(id+"-r", "Standard",2,BigDecimal.valueOf(price),3)));
    }
}
