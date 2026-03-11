package com.nataliya.validation;

import com.nataliya.dto.resource.MovingResourceRequestDto;
import com.nataliya.model.ResourceType;
import com.nataliya.util.PathUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MovePathValidator implements ConstraintValidator<ValidMovePath, MovingResourceRequestDto> {

    @Override
    public boolean isValid(MovingResourceRequestDto dto, ConstraintValidatorContext context) {

        if (dto.from() == null || dto.to() == null) {
            return true;
        }

        String fromPath = dto.from();
        String toPath = dto.to();

        if (fromPath.equals(toPath)) {
            return buildViolation(context, "Source and destination paths must be different");
        }

        if (toPath.startsWith(fromPath + "/")) {
            return buildViolation(context, "Cannot move directory into its own subtree");
        }

        ResourceType fromResourceType =
                PathUtil.isDirectory(fromPath) ? ResourceType.DIRECTORY : ResourceType.FILE;
        ResourceType toResourceType =
                PathUtil.isDirectory(toPath) ? ResourceType.DIRECTORY : ResourceType.FILE;

        if (fromResourceType != toResourceType) {
            return buildViolation(context, "Cannot move file to directory or directory to file");
        }

        String fromParent = PathUtil.extractParentDirectoryPath(fromPath);
        String fromName = PathUtil.extractResourceName(fromPath, false);

        String toParent = PathUtil.extractParentDirectoryPath(toPath);
        String toName = PathUtil.extractResourceName(toPath, false);

        boolean parentChanged = !fromParent.equals(toParent);
        boolean nameChanged = !fromName.equals(toName);

        if (parentChanged == nameChanged) { // XOR
            return buildViolation(context,
                    "Move operation must change either parent directory or resource name, but not both"
            );
        }
        return true;
    }

    private boolean buildViolation(ConstraintValidatorContext context, String message) {

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();

        return false;
    }
}
