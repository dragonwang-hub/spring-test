package com.thoughtworks.rslist.domain;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Trade {
    private int amount;
    private int rank;
}
