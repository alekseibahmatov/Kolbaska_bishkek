package ee.kolbaska.kolbaska.mapper;

import ee.kolbaska.kolbaska.model.login.Login;
import ee.kolbaska.kolbaska.response.LoginResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Stream;

@Mapper
public interface LoginMapper {
    LoginMapper INSTANCE = Mappers.getMapper(LoginMapper.class);

    List<LoginResponse> toLoginResponse(Stream<Login> loginStream);
}
