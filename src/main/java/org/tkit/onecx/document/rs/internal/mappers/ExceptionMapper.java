package org.tkit.onecx.document.rs.internal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.tkit.onecx.document.rs.internal.exception.RestException;

import gen.org.tkit.onecx.document.rs.internal.model.ProblemDetailResponseDTO;

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
