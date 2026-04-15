package org.tkit.onecx.document.rs.internal.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.tkit.onecx.document.domain.models.entities.DocumentSpecification;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.document.rs.internal.model.DocumentSpecificationCreateUpdateDTO;
import gen.org.tkit.onecx.document.rs.internal.model.DocumentSpecificationDTO;

@Mapper(uses = OffsetDateTimeMapper.class)
public interface DocumentSpecificationMapper {
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    DocumentSpecification map(DocumentSpecificationCreateUpdateDTO dto);

    DocumentSpecificationDTO mapToDTO(DocumentSpecification documentSpecification);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    void update(DocumentSpecificationCreateUpdateDTO dto, @MappingTarget DocumentSpecification documentSpecification);

    List<DocumentSpecificationDTO> findAllDocumentSpecifications(List<DocumentSpecification> documentSpecifications);
}
