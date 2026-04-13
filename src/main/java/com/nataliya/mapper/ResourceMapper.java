package com.nataliya.mapper;

import com.nataliya.dto.response.resource.ResourceResponseDto;
import com.nataliya.model.ResourceType;
import com.nataliya.model.entity.Resource;
import com.nataliya.util.PathUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ResourceMapper {

    @Mapping(target = "path", source = "resource", qualifiedByName = "toParentPath")
    @Mapping(target = "name", source = "resource", qualifiedByName = "toDisplayName")
    @Mapping(source = "resourceType", target = "type")
    ResourceResponseDto resourceToResourceDto(Resource resource);

    List<ResourceResponseDto> resourceListToDtoList(List<Resource> resources);

    @Named("toParentPath")
    default String toParentPath(Resource resource) {
        return PathUtil.extractParentDirectoryPath(resource.getPath());
    }

    @Named("toDisplayName")
    default String toDisplayName(Resource resource) {
        if (resource.getResourceType() == ResourceType.DIRECTORY) {
            return resource.getResourceName() + "/";
        }
        return resource.getResourceName();
    }
}
