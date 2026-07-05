package co.com.nequi.dynamodb.ticket;

import co.com.nequi.model.ticket.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketDynamoDBAdapterTest {

    private static final String TABLE_NAME = "Tickets";

    @Mock
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private DynamoDbAsyncTable<TicketEntity> ticketTable;

    private TicketDynamoDBAdapter adapter;

    @BeforeEach
    void setUp() {
        when(dynamoDbEnhancedAsyncClient.table(TABLE_NAME, TableSchema.fromBean(TicketEntity.class)))
                .thenReturn(ticketTable);

        adapter = new TicketDynamoDBAdapter(dynamoDbEnhancedAsyncClient, mapper, TABLE_NAME);
    }

    @Test
    void shouldAggregateTicketsAcrossMultiplePages() {
        TicketEntity pageOneTicket = entity("t1", "event-1", null, "AVAILABLE");
        TicketEntity pageTwoTicket = entity("t2", "event-1", null, "SOLD");

        Page<TicketEntity> pageOne = Page.builder(TicketEntity.class).items(List.of(pageOneTicket)).build();
        Page<TicketEntity> pageTwo = Page.builder(TicketEntity.class).items(List.of(pageTwoTicket)).build();

        when(ticketTable.query(any(QueryEnhancedRequest.class)))
                .thenReturn(PagePublisher.create(SdkPublisher.adapt(Flux.just(pageOne, pageTwo))));

        StepVerifier.create(adapter.findByEventId("event-1"))
                .expectNextMatches(t -> t.getTicketId().equals("t1") && t.getStatus() == TicketStatus.AVAILABLE)
                .expectNextMatches(t -> t.getTicketId().equals("t2") && t.getStatus() == TicketStatus.SOLD)
                .verifyComplete();
    }

    private static TicketEntity entity(String ticketId, String eventId, String orderId, String status) {
        TicketEntity entity = new TicketEntity();
        entity.setTicketId(ticketId);
        entity.setEventId(eventId);
        entity.setOrderId(orderId);
        entity.setStatus(status);
        entity.setVersion(1L);
        entity.setReservedAt(Instant.parse("2026-07-01T10:00:00Z").toString());
        return entity;
    }
}
