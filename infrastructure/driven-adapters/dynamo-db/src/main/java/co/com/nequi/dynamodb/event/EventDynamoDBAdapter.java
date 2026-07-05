package co.com.nequi.dynamodb.event;

import co.com.nequi.model.event.Event;
import co.com.nequi.model.event.gateways.EventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.time.Instant;

@Repository
public class EventDynamoDBAdapter implements EventRepository {

    private final DynamoDbAsyncTable<EventEntity> table;

    public EventDynamoDBAdapter(DynamoDbEnhancedAsyncClient client,
                                 @Value("${adapter.dynamodb.tickets-table-name}") String ticketsTableName) {
        this.table = client.table(ticketsTableName, TableSchema.fromBean(EventEntity.class));
    }

    @Override
    public Mono<Event> findById(String eventId) {
        Key key = Key.builder().partitionValue(eventId).sortValue(EventEntity.METADATA_SORT_KEY).build();
        return Mono.fromFuture(table.getItem(key)).map(EventDynamoDBAdapter::toDomain);
    }

    private static Event toDomain(EventEntity entity) {
        return Event.builder()
                .eventId(entity.getEventId())
                .name(entity.getName())
                .venue(entity.getVenue())
                .date(Instant.parse(entity.getDate()))
                .totalCapacity(entity.getTotalCapacity())
                .build();
    }
}
