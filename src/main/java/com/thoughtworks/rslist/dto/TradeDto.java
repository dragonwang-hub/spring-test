package com.thoughtworks.rslist.dto;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "trade")
public class TradeDto {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private int amount;

    private int rank;

    @OneToOne(optional = false, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "rsEvent_id")
    private RsEventDto rsEvent;
}
