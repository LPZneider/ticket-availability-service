package co.com.nequi.dynamodb.ticket;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/* Enhanced DynamoDB annotations are incompatible with Lombok #1932
   https://github.com/aws/aws-sdk-java-v2/issues/1932 */
/**
 * Single-table item read from TICKETS_TABLE_NAME: pk=eventId, sk=ticketId
 * (Event metadata shares the partition with sk="METADATA" and is filtered out
 * at query time in TicketDynamoDBAdapter). Matches the physical key names
 * written by ticket-reservation-service/ticket-purchase-service/reservation-expiry-service.
 */
@DynamoDbBean
public class TicketEntity {

    private String pk;
    private String sk;
    private String orderId;
    private String status;
    private long version;
    private String reservedAt;
    private Long reservationExpiresAt;

    public TicketEntity() {
    }

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
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
