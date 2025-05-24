package com.olg.kafka.consumers;

import com.olg.kafka.BaseKafkaMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class GenericKafkaConsumer {

    @KafkaListener(topics = "my-topic", groupId = "generic-consumer", containerFactory = "baseMessageListenerFactory")
    public void listen(BaseKafkaMessage<Object> message) {
        System.out.println("Received: " + message.getEventType());

        switch (message.getEventType()) {
//            case "booking":
//                BookingPayload booking = convert(message.getPayload(), BookingPayload.class);
//                System.out.println("Booking payload: " + booking.getId());
//                break;
//            case "payment":
//                PaymentPayload payment = convert(message.getPayload(), PaymentPayload.class);
//                System.out.println("Payment payload: " + payment.getAmount());
//                break;
            default:
                System.out.println("Unknown event: " + message.getEventType());
        }
    }

//    private <T> T convert(Object obj, Class<T> targetClass) {
//        return new ObjectMapper().convertValue(obj, targetClass);
//    }
}

