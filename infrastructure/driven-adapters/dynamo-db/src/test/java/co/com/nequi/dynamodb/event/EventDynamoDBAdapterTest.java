package co.com.nequi.dynamodb.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventDynamoDBAdapterTest {

    @Mock private DynamoDbEnhancedAsyncClient client;
    @Mock private DynamoDbAsyncTable<EventEntity> table;

    private EventDynamoDBAdapter adapter;

    @BeforeEach
    void setUp() {
        when(client.table("tickets", TableSchema.fromBean(EventEntity.class))).thenReturn(table);
        adapter = new EventDynamoDBAdapter(client, "tickets");
    }

    private EventEntity entity(int total, int available) {
        EventEntity e = new EventEntity();
        e.setPk("event-1"); e.setSk(EventEntity.METADATA_SORT_KEY);
        e.setEventId("event-1"); e.setName("Concert");
        e.setDate("2026-08-01T20:00:00Z"); e.setVenue("Main Arena");
        e.setTotalCapacity(total); e.setAvailableCount(available);
        return e;
    }

    @Test
    void shouldFindEventAndMapAvailableCount() {
        Key key = Key.builder().partitionValue("event-1").sortValue(EventEntity.METADATA_SORT_KEY).build();
        when(table.getItem(key)).thenReturn(CompletableFuture.completedFuture(entity(100, 75)));

        StepVerifier.create(adapter.findById("event-1"))
                .assertNext(event -> {
                    assertThat(event.getEventId()).isEqualTo("event-1");
                    assertThat(event.getTotalCapacity()).isEqualTo(100);
                    assertThat(event.getAvailableCount()).isEqualTo(75);
                })
                .verifyComplete();
    }

    @Test
    void shouldMapZeroAvailableCount() {
        Key key = Key.builder().partitionValue("event-1").sortValue(EventEntity.METADATA_SORT_KEY).build();
        when(table.getItem(key)).thenReturn(CompletableFuture.completedFuture(entity(50, 0)));

        StepVerifier.create(adapter.findById("event-1"))
                .assertNext(event -> assertThat(event.getAvailableCount()).isEqualTo(0))
                .verifyComplete();
    }

    @Test
    void shouldCompleteEmptyWhenEventNotFound() {
        Key key = Key.builder().partitionValue("missing").sortValue(EventEntity.METADATA_SORT_KEY).build();
        when(table.getItem(key)).thenReturn(CompletableFuture.completedFuture(null));

        StepVerifier.create(adapter.findById("missing")).verifyComplete();
    }
}
