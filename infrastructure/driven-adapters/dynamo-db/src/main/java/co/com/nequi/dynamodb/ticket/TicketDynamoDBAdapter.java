package co.com.nequi.dynamodb.ticket;

import co.com.nequi.dynamodb.helper.TemplateAdapterOperations;
import co.com.nequi.model.ticket.Ticket;
import co.com.nequi.model.ticket.TicketStatus;
import co.com.nequi.model.ticket.gateways.TicketRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.Instant;

@Repository
public class TicketDynamoDBAdapter
        extends TemplateAdapterOperations<Ticket, String, TicketEntity>
        implements TicketRepository {

    public TicketDynamoDBAdapter(DynamoDbEnhancedAsyncClient connectionFactory,
                                  ObjectMapper mapper,
                                  @Value("${adapter.dynamodb.tickets-table-name}") String tableName) {
        super(connectionFactory, mapper, TicketDynamoDBAdapter::toDomain, tableName);
    }

    @Override
    public Flux<Ticket> findByEventId(String eventId) {
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(eventId).build()))
                .build();
        return query(request).flatMapMany(Flux::fromIterable);
    }

    private static Ticket toDomain(TicketEntity entity) {
        return Ticket.builder()
                .ticketId(entity.getTicketId())
                .eventId(entity.getEventId())
                .orderId(entity.getOrderId())
                .status(TicketStatus.valueOf(entity.getStatus()))
                .version(entity.getVersion())
                .reservedAt(entity.getReservedAt() != null ? Instant.parse(entity.getReservedAt()) : null)
                .reservationExpiresAt(entity.getReservationExpiresAt() != null
                        ? Instant.ofEpochSecond(entity.getReservationExpiresAt()) : null)
                .build();
    }
}
