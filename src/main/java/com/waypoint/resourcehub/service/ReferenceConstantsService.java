package com.waypoint.resourcehub.service;

import static com.waypoint.resourcehub.constants.ResourceHubConstants.*;

import com.waypoint.resourcehub.domain.AssetDTO;
import com.waypoint.resourcehub.domain.RefSetDTO;
import com.waypoint.resourcehub.domain.RefTermDTO;
import com.waypoint.resourcehub.domain.entity.Asset;
import com.waypoint.resourcehub.domain.response.ReferenceConstantsResponse;
import com.waypoint.resourcehub.exception.ErrorMessage;
import com.waypoint.resourcehub.exception.GenericException;
import com.waypoint.resourcehub.repository.AssetRepository;
import com.waypoint.resourcehub.repository.RefSetRepository;
import com.waypoint.resourcehub.repository.RefSetTermViewRepository;
import com.waypoint.resourcehub.repository.RefTermRepository;
import com.waypoint.resourcehub.utilities.ReferenceConstantsMapper;
import java.io.File;
import java.util.Collections;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@Slf4j
public class ReferenceConstantsService {
  private final RefSetRepository refSetRepository;
  private final RefTermRepository refTermRepository;
  private final RefSetTermViewRepository refSetTermViewRepository;
  private final AssetRepository assetRepository;
  private final S3Service s3Service;

  public ReferenceConstantsService(
      RefSetRepository refSetRepository,
      RefTermRepository refTermRepository,
      RefSetTermViewRepository refSetTermViewRepository,
      AssetRepository assetRepository,
      S3Service s3Service) {
    this.refSetRepository = refSetRepository;
    this.refTermRepository = refTermRepository;
    this.refSetTermViewRepository = refSetTermViewRepository;
    this.assetRepository = assetRepository;
    this.s3Service = s3Service;
  }

  public Flux<RefSetDTO> getRefSetDetails(RefSetDTO refSetDTO) {
    log.info("getRefSetDetails :: START");
    if (refSetDTO.getId() != null) {
      return refSetRepository
          .findByIdAndIsActive(refSetDTO.getId(), true)
          .map(refSet -> ReferenceConstantsMapper.getRefSetDTO(refSet, refSetDTO.getRefTermList()))
          .flux();
    } else if (refSetDTO.getRefSetId() != null) {
      return refSetRepository
          .findByRefSetIdAndIsActive(refSetDTO.getRefSetId(), true)
          .map(refSet -> ReferenceConstantsMapper.getRefSetDTO(refSet, refSetDTO.getRefTermList()))
          .flux();
    } else if (!StringUtils.isBlank(refSetDTO.getRefSetName())) {
      return refSetRepository
          .findByRefSetNameLikeAndIsActive(
              PERCENTILE_SIGN + refSetDTO.getRefSetName() + PERCENTILE_SIGN, true)
          .map(refSet -> ReferenceConstantsMapper.getRefSetDTO(refSet, refSetDTO.getRefTermList()));
    }
    return Flux.just(refSetDTO);
  }

  public Flux<RefSetDTO> getRefTermDetails(RefSetDTO refSetDTO) {
    log.info("getRefTermDetails :: START");
    if (refSetDTO.getId() != null
        || refSetDTO.getRefSetId() != null
        || !StringUtils.isBlank(refSetDTO.getRefSetName())) {

      if (!CollectionUtils.isEmpty(refSetDTO.getRefTermList())) {
        return Flux.fromIterable(refSetDTO.getRefTermList())
            .concatMap(
                refTermDTO -> {
                  if (refTermDTO.getId() != null) {
                    return refTermRepository
                        .findByIdAndRefSetIdAndIsActive(refTermDTO.getId(), refSetDTO.getId(), true)
                        .map(ReferenceConstantsMapper::getRefTermDTO)
                        .flux();
                  } else if (refTermDTO.getRefTermId() != null) {
                    return refTermRepository
                        .findByRefTermIdAndRefSetIdAndIsActive(
                            refTermDTO.getRefTermId(), refSetDTO.getId(), true)
                        .map(ReferenceConstantsMapper::getRefTermDTO)
                        .flux();
                  } else if (!StringUtils.isEmpty(refTermDTO.getRefTermName())) {
                    return refTermRepository
                        .findAllByRefTermNameLikeAndRefSetIdAndIsActive(
                            PERCENTILE_SIGN + refTermDTO.getRefTermName() + PERCENTILE_SIGN,
                            refSetDTO.getId(),
                            true)
                        .map(ReferenceConstantsMapper::getRefTermDTO);
                  }
                  return Flux.empty();
                })
            .collectList()
            .map(refSetDTO::withRefTermList)
            .flux();
      } else {
        return refTermRepository
            .findAllByRefSetIdAndIsActive(refSetDTO.getId(), true)
            .map(ReferenceConstantsMapper::getRefTermDTO)
            .collectList()
            .map(refSetDTO::withRefTermList)
            .flux();
      }
    } else {
      return Flux.fromIterable(refSetDTO.getRefTermList())
          .concatMap(
              refTermDTO -> {
                if (refTermDTO.getId() != null) {
                  return refTermRepository
                      .findByIdAndIsActive(refTermDTO.getId(), true)
                      .map(ReferenceConstantsMapper::getRefTermDTO)
                      .flatMap(
                          refTermDTO1 ->
                              refSetRepository
                                  .findByIdAndIsActive(refTermDTO1.getRefSetId(), true)
                                  .map(
                                      refSet ->
                                          ReferenceConstantsMapper.getRefSetDTO(
                                              refSet, Collections.singletonList(refTermDTO1))))
                      .flux();
                } else if (refTermDTO.getRefTermId() != null) {
                  return refTermRepository
                      .findByRefTermIdAndIsActive(refTermDTO.getRefTermId(), true)
                      .map(ReferenceConstantsMapper::getRefTermDTO)
                      .flatMap(
                          refTermDTO1 ->
                              refSetRepository
                                  .findByIdAndIsActive(refTermDTO1.getRefSetId(), true)
                                  .map(
                                      refSet ->
                                          ReferenceConstantsMapper.getRefSetDTO(
                                              refSet, Collections.singletonList(refTermDTO1))))
                      .flux();
                } else if (!StringUtils.isEmpty(refTermDTO.getRefTermName())) {
                  return refTermRepository
                      .findByRefTermNameLikeAndIsActive(
                          PERCENTILE_SIGN + refTermDTO.getRefTermName() + PERCENTILE_SIGN, true)
                      .map(ReferenceConstantsMapper::getRefTermDTO)
                      .groupBy(RefTermDTO::getRefSetId)
                      .flatMap(
                          groupedFlux ->
                              groupedFlux
                                  .collectList()
                                  .flatMap(
                                      refTermDTOList ->
                                          refSetRepository
                                              .findByIdAndIsActive(groupedFlux.key(), true)
                                              .map(
                                                  refSet ->
                                                      ReferenceConstantsMapper.getRefSetDTO(
                                                          refSet, refTermDTOList))));
                }
                return Flux.empty();
              });
    }
  }

  public Mono<ReferenceConstantsResponse> getReferenceConstantsDetails(RefSetDTO refSetDTO) {
    log.info("getReferenceConstantsDetails :: refSetDTO :: {}", refSetDTO);
    return Mono.just(refSetDTO)
        .flatMapMany(this::getRefSetDetails)
        .flatMap(this::getRefTermDetails)
        .collectList()
        .map(
            refSetDTOList ->
                ReferenceConstantsResponse.builder().refSetList(refSetDTOList).build());
  }

  public Mono<AssetDTO> uploadAsset(UUID assetTypeId, File asset) {
    return refSetTermViewRepository
        .findById(assetTypeId)
        .switchIfEmpty(Mono.error(new GenericException(ErrorMessage.INVALID_ASSET_TYPE_ID)))
        .flatMap(
            refSetTermView -> {
              // Generate a unique file name
              String newFileName = UUID.randomUUID() + "_" + asset.getName();
              log.info(
                  "uploadAsset :: Uploaded File Name :: {} :: New File Name :: {}",
                  asset.getName(),
                  newFileName);
              String s3Key =
                  S3_ASSET_BASE_PATH
                      + refSetTermView.getRefSetName()
                      + FORWARD_SLASH
                      + refSetTermView.getRefTermName()
                      + FORWARD_SLASH
                      + newFileName;
              return s3Service
                  .uploadFile(asset, s3Key)
                  .then(
                      assetRepository
                          .save(
                              ReferenceConstantsMapper.getAsset(
                                  refSetTermView.getRefTermUUID(),
                                  s3Key,
                                  newFileName,
                                  ReferenceConstantsMapper.getFileExtension(asset)))
                          .map(Asset::getId)
                          .flatMap(assetRepository::findById)
                          .map(ReferenceConstantsMapper::getAssetDTO)
                          .switchIfEmpty(
                              s3Service
                                  .deleteFile(s3Key)
                                  .then(
                                      Mono.error(
                                          new GenericException(ErrorMessage.ASSET_SAVE_FAILED)))));
            });
  }

  public Mono<Tuple2<byte[], AssetDTO>> getAsset(UUID assetId) {
    log.info("getAsset :: assetId :: {}", assetId);
    return assetRepository
        .findByIdAndIsActive(assetId, true)
        .map(ReferenceConstantsMapper::getAssetDTO)
        .switchIfEmpty(
            Mono.error(new GenericException(ErrorMessage.MANDATORY_FIELDS_MISSING_OR_INVALID)))
        .flatMap(
            assetDTO -> {
              log.info("getAsset :: assetDTO :: {}", assetDTO);
              return s3Service.getFile(assetDTO.getAssetPath()).zipWith(Mono.just(assetDTO));
            })
        .switchIfEmpty(
            Mono.error(new GenericException(ErrorMessage.ASSET_RETRIEVAL_FROM_S3_FAILED)));
  }
}
