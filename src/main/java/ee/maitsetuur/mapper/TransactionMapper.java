package ee.maitsetuur.mapper;

import ee.maitsetuur.model.transaction.Transaction;
import ee.maitsetuur.response.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);
    List<TransactionResponse> toTransactionResponseList(List<Transaction> transactions);

    default TransactionResponse toTransactionResponseList(Transaction transaction) {

        return TransactionResponse.builder()
                .id(transaction.getId().toString())
                .value(String.format("%.2f", transaction.getValue()))
                .restaurantCode(transaction.getRestaurant().getRestaurantCode())
                .restaurantName(transaction.getRestaurant().getName())
                .waiterId(transaction.getWaiter().getId())
                .waiterEmail(transaction.getWaiter().getEmail())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
