package com.thoughtworks.rslist.domain;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RsEvent implements Serializable {
  @NotNull private String eventName;
  @NotNull private String keyword;
  private int voteNum;
  @NotNull private int userId;

  private int rsRank;
}
