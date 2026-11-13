package com.stayease.service;

import com.stayease.dto.BookingRequest;
import com.stayease.dto.BookingResponse;
import com.stayease.entity.BookingStatus;
import com.stayease.entity.Room;
import com.stayease.repository.BookingRepository;
import com.stayease.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @ActiveProfiles("mysql") @EnabledIfEnvironmentVariable(named="RUN_MYSQL_INTEGRATION_TESTS",matches="true")
class MySqlIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired BookingService service;
    @Autowired RoomRepository rooms;
    @Autowired BookingRepository bookings;
    private String createdReference;
    private String createdEmail;

    @Test void mysqlSchemaFlywayAndSampleInventoryArePresent(){
        String version=jdbc.queryForObject("select version()",String.class);
        assertNotNull(version);assertTrue(version.startsWith("8."),"Expected MySQL 8.x but got "+version);
        assertEquals(3,jdbc.queryForObject("select count(*) from information_schema.tables where table_schema=database() and table_name in ('hotels','rooms','bookings')",Integer.class));
        assertEquals(0,jdbc.queryForObject("select count(*) from information_schema.tables where table_schema=database() and table_name='hotel_amenities'",Integer.class));
        assertEquals(3,jdbc.queryForObject("select count(*) from flyway_schema_history where success=1 and version in ('1','2','3')",Integer.class));
        assertEquals(13,jdbc.queryForObject("select count(*) from hotels",Integer.class));
        assertEquals(10,jdbc.queryForObject("select count(*) from hotels where area in ('Mumbai','Pune','Goa','Delhi','Bengaluru')",Integer.class));
        assertEquals("json",jdbc.queryForObject("select data_type from information_schema.columns where table_schema=database() and table_name='hotels' and column_name='amenities'",String.class));
        assertTrue(jdbc.queryForObject("select count(*) from information_schema.key_column_usage where constraint_schema=database() and table_name in ('rooms','bookings') and referenced_table_name is not null",Integer.class)>=2);
        assertTrue(jdbc.queryForObject("select count(*) from rooms",Integer.class)>=13);
    }

    @Test @Transactional void mysqlBookingLookupCancellationAndRollbackWork(){
        Room room=rooms.findById("h1-r1").orElseThrow();LocalDate checkIn=LocalDate.now().plusDays(3000);createdEmail="mysql-verify-"+UUID.randomUUID()+"@example.test";
        BookingRequest request=new BookingRequest("h1",null,room.getId(),"Database Verification",createdEmail,checkIn,checkIn.plusDays(2),1);
        BookingResponse created=service.create(request);createdReference=created.reference();
        assertEquals(room.getPricePerNight().multiply(java.math.BigDecimal.valueOf(2)).setScale(2),created.total());
        assertEquals("Confirmed",service.find(created.reference(),createdEmail).status());
        assertEquals("Cancelled",service.cancel(created.reference(),createdEmail).status());
        assertEquals(0,bookings.countOverlapping(room.getId(),BookingStatus.CONFIRMED,checkIn,checkIn.plusDays(2)));
    }

    @AfterTransaction void bookingIntegrationFixtureWasRolledBack(){if(createdReference!=null)assertTrue(bookings.findByBookingReferenceIgnoreCaseAndCustomerEmailIgnoreCase(createdReference,createdEmail).isEmpty());}
}
