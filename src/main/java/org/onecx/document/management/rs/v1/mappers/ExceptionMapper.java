package org.onecx.document.management.rs.v1.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.onecx.document.management.rs.v1.exception.RestException;

import gen.org.onecx.document.management.rs.v1.model.ProblemDetailResponseDTO;

@Mapper()
public interface ExceptionMapper {

    @Mapping(target = "invalidParams", ignore = true)
    @Mapping(target = "removeInvalidParamsItem", ignore = true)
    @Mapping(target = "removeParamsItem", ignore = true)
    @Mapping(target = "params", ignore = true)
    @Mapping(target = "detail", source = "message")
    @Mapping(target = "errorCode", qualifiedByName = "mapErrorCode")
    ProblemDetailResponseDTO map(RestException restException);

    @Named("mapErrorCode")
    default String mapErrorCode(Enum<?> errorCode) {
        return errorCode.name();
    }
}
