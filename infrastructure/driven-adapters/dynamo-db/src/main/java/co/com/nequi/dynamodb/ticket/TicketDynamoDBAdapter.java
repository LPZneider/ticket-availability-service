package co.com.nequi.dynamodb.ticket;

import co.com.nequi.dynamodb.event.EventEntity;
import co.com.nequi.model.ticket.Ticket;
import co.com.nequi.model.ticket.TicketStatus;
import co.com.nequi.model.ticket.gateways.TicketRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.Instant;

@Repository
public class TicketDynamoDBAdapter implements TicketRepository {

    private final DynamoDbAsyncTable<TicketEntity> table;

    public TicketDynamoDBAdapter(DynamoDbEnhancedAsyncClient client,
                                  @Value("${adapter.dynamodb.tickets-table-name}") String ticketsTableName) {
        this.table = client.table(ticketsTableName, TableSchema.fromBean(TicketEntity.class));
    }

    @Override
    public Flux<Ticket> findByEventId(String eventId) {
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(eventId).build()))
                .build();
        // DynamoDB rejects filter expressions on primary key attributes (sk), so the
        // co-located Event metadata row is excluded client-side instead of server-side.
        return Flux.from(table.query(request))
                .concatMap(page -> Flux.fromIterable(page.items()))
                .filter(entity -> !EventEntity.METADATA_SORT_KEY.equals(entity.getSk()))
                .map(TicketDynamoDBAdapter::toDomain);
    }

    private static Ticket toDomain(TicketEntity entity) {
        return Ticket.builder()
                .ticketId(entity.getSk())
                .eventId(entity.getPk())
                .orderId(entity.getOrderId())
                .status(TicketStatus.valueOf(entity.getStatus()))
                .version(entity.getVersion())
                .reservedAt(entity.getReservedAt() != null ? Instant.parse(entity.getReservedAt()) : null)
                .reservationExpiresAt(entity.getReservationExpiresAt() != null
                        ? Instant.ofEpochSecond(entity.getReservationExpiresAt()) : null)
                .build();
    }
}
