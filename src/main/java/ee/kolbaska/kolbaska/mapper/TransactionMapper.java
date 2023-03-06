package ee.kolbaska.kolbaska.mapper;

import ee.kolbaska.kolbaska.model.transaction.Transaction;
import ee.kolbaska.kolbaska.response.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Stream;

@Mapper
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(source = "certificate.id", target = "certificateId")
    List<TransactionResponse> toTransactionResponse(Stream<Transaction> transactionStream);
}
