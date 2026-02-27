package com.nataliya.mapper;

import com.nataliya.dto.resource.ResourceResponseDto;
import com.nataliya.model.ResourceType;
import com.nataliya.model.entity.Resource;
import com.nataliya.util.PathUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = PathUtil.class)
public interface ResourceMapper {

    @Mapping(
            target = "path",
            expression = "java(PathUtil.extractParentDirectoryPath(resource.getPath()))"
    )
    @Mapping(target = "name", expression = "java(mapName(resource))")
    @Mapping(source = "resourceType", target = "type")
    ResourceResponseDto resourceToResourceDto(Resource resource);

    List<ResourceResponseDto> resourceListToDtoList(List<Resource> resources);

    default String mapName(Resource resource) {
        if (resource.getResourceType() == ResourceType.DIRECTORY) {
            return resource.getResourceName() + "/";
        }
        return resource.getResourceName();
    }
}
