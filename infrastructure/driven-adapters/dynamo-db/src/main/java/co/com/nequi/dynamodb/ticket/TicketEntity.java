package co.com.nequi.dynamodb.ticket;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/* Enhanced DynamoDB annotations are incompatible with Lombok #1932
   https://github.com/aws/aws-sdk-java-v2/issues/1932 */
@DynamoDbBean
public class TicketEntity {

    private String eventId;
    private String ticketId;
    private String orderId;
    private String status;
    private long version;
    private String reservedAt;
    private Long reservationExpiresAt;

    public TicketEntity() {
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("eventId")
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("ticketId")
    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    @DynamoDbAttribute("orderId")
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @DynamoDbAttribute("status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @DynamoDbAttribute("version")
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @DynamoDbAttribute("reservedAt")
    public String getReservedAt() {
        return reservedAt;
    }

    public void setReservedAt(String reservedAt) {
        this.reservedAt = reservedAt;
    }

    @DynamoDbAttribute("reservationExpiresAt")
    public Long getReservationExpiresAt() {
        return reservationExpiresAt;
    }

    public void setReservationExpiresAt(Long reservationExpiresAt) {
        this.reservationExpiresAt = reservationExpiresAt;
    }
}
