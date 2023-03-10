package ee.kolbaska.kolbaska.mapper;

import ee.kolbaska.kolbaska.model.transaction.Transaction;
import ee.kolbaska.kolbaska.response.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);
    List<TransactionResponse> toTransactionResponseList(List<Transaction> transactions);

    default TransactionResponse toTransactionResponseList(Transaction transaction) {

        return TransactionResponse.builder()
                .id(transaction.getId())
                .value(transaction.getValue())
                .restaurantCode(transaction.getRestaurant().getRestaurantCode())
                .restaurantName(transaction.getRestaurant().getName())
                .certificateId(transaction.getCertificate().getId())
                .build();
    }
}
