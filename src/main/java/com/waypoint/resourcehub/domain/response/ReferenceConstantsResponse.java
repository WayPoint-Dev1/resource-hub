package com.waypoint.resourcehub.domain.response;

import com.waypoint.resourcehub.domain.RefSetDTO;
import com.waypoint.resourcehub.domain.RefTermDTO;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ReferenceConstantsResponse {
  private List<RefSetDTO> refSetList;
}
