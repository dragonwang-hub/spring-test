package com.thoughtworks.rslist.dto;

import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rsEvent")
public class RsEventDto {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private String eventName;
    private String keyword;
    private int voteNum;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserDto user;

    @OneToOne(cascade = CascadeType.ALL,mappedBy = "rsEvent")
    @JoinColumn(name = "trade_id")
    private TradeDto trade;

    @Column(unique = true)
    private int rsRank;
}
