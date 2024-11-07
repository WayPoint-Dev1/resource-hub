package com.waypoint.resourcehub.utilities;

import static com.waypoint.resourcehub.constants.ResourceHubConstants.DEF_USER_NAME;

import com.waypoint.resourcehub.domain.AssetDTO;
import com.waypoint.resourcehub.domain.RefSetDTO;
import com.waypoint.resourcehub.domain.RefTermDTO;
import com.waypoint.resourcehub.domain.entity.Asset;
import com.waypoint.resourcehub.domain.entity.RefSet;
import com.waypoint.resourcehub.domain.entity.RefTerm;
import com.waypoint.resourcehub.exception.ErrorMessage;
import com.waypoint.resourcehub.exception.GenericException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@Slf4j
public class ReferenceConstantsMapper {

  public static Mono<RefSetDTO> validateGetReferenceConstantsDetailsRequest(RefSetDTO refSetDTO) {
    boolean isRefTermListPresent =
        !(CollectionUtils.isEmpty(refSetDTO.getRefTermList())
            || !validateRefTermDTOList(refSetDTO.getRefTermList()));
    if (refSetDTO.getId() != null
        || refSetDTO.getRefSetId() != null
        || !StringUtils.isBlank(refSetDTO.getRefSetName())
        || isRefTermListPresent) {
      refSetDTO.setRefTermList(isRefTermListPresent ? refSetDTO.getRefTermList() : null);
      return Mono.just(refSetDTO);
    }
    return Mono.error(new GenericException(ErrorMessage.MANDATORY_FIELDS_MISSING_OR_INVALID));
  }

  public static boolean validateRefTermDTOList(List<RefTermDTO> refTermDTOList) {
    return refTermDTOList.stream()
        .anyMatch(
            refTermDTO ->
                refTermDTO.getId() != null
                    || refTermDTO.getRefTermId() != null
                    || !StringUtils.isBlank(refTermDTO.getRefTermName()));
  }

  public static RefSetDTO getRefSetDTO(RefSet refSet, List<RefTermDTO> refTermDTOList) {
    return RefSetDTO.builder()
        .id(refSet.getId())
        .refSetId(refSet.getRefSetId())
        .refSetName(refSet.getRefSetName())
        .refTermList(refTermDTOList)
        .build();
  }

  public static RefTermDTO getRefTermDTO(RefTerm refTerm) {
    return RefTermDTO.builder()
        .id(refTerm.getId())
        .refTermId(refTerm.getRefTermId())
        .refSetId(refTerm.getRefSetId())
        .refTermName(refTerm.getRefTermName())
        .build();
  }

  public static Mono<MultiValueMap<String, Part>> validateUploadAssetRequest(
      MultiValueMap<String, Part> multiValueMap) {

    if (multiValueMap.toSingleValueMap().get("file") != null
        && multiValueMap.toSingleValueMap().get("assetTypeId") != null) {
      return Mono.just(multiValueMap);
    }
    return Mono.error(new GenericException(ErrorMessage.MANDATORY_FIELDS_MISSING_OR_INVALID));
  }

  public static String getFileExtension(File file) {

    Path path = file.toPath();
    String mimeType;
    try {
      mimeType = Files.probeContentType(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    log.info("mimeType :: {}", mimeType);
    if (mimeType != null) {
      return mimeType.split("/")[1]; // Extract the extension part from MIME type
    } else {
      return ""; // Handle cases where the MIME type is not determined
    }
  }

  public static Asset getAsset(
      UUID assetTypeId, String assetPath, String assetName, String extensionType) {
    return Asset.builder()
        .assetPath(assetPath)
        .assetTypeId(assetTypeId)
        .assetName(assetName)
        .extensionType(extensionType)
        .isActive(true)
        .createdBy(DEF_USER_NAME)
        .createdOn(LocalDateTime.now())
        .updatedBy(DEF_USER_NAME)
        .updatedOn(LocalDateTime.now())
        .build();
  }

  public static AssetDTO getAssetDTO(Asset asset) {
    log.info("getAssetDTO :: Asset :: {}", asset);
    return AssetDTO.builder()
        .id(asset.getId())
        .assetId(asset.getAssetId())
        .assetPath(asset.getAssetPath())
        .assetTypeId(asset.getAssetTypeId())
        .assetName(asset.getAssetName())
        .extensionType(asset.getExtensionType())
        .isActive(asset.getIsActive())
        .build();
  }

  public static Mono<UUID> validateGetAssetRequest(ServerRequest serverRequest) {
    String assetId = serverRequest.pathVariable("assetId");
    if (StringUtils.isNotBlank(assetId)) {
      try {
        return Mono.just(UUID.fromString(assetId));
      } catch (IllegalArgumentException e) {
        return Mono.error(new GenericException(ErrorMessage.MANDATORY_FIELDS_MISSING_OR_INVALID));
      }
    }
    return Mono.error(new GenericException(ErrorMessage.MANDATORY_FIELDS_MISSING_OR_INVALID));
  }

  public static MediaType determineMediaType(String extension) {
    return switch (extension.toLowerCase()) {
      case "png" -> MediaType.IMAGE_PNG;
      case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
      case "pdf" -> MediaType.APPLICATION_PDF;
      case "txt" -> MediaType.TEXT_PLAIN;
      default -> MediaType.APPLICATION_OCTET_STREAM;
    };
  }
}
