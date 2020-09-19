package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "trade")
public class TradeDto {
    @Id
    @GeneratedValue
    private int id;
    private int amount;
    // 尝试加入@Column(unique = true)
    private int rank;

    @OneToOne(optional = false, mappedBy = "trade", cascade = CascadeType.REMOVE)
    @JoinColumn(name = "rsEvent_id")
    private RsEventDto rsEvent;
}
