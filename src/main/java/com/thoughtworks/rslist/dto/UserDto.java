package com.thoughtworks.rslist.dto;

import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    @Column(name = "name")
    private String userName;
    private String gender;
    private int age;
    private String email;
    private String phone;
    @Builder.Default
    private int voteNum =10;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "user")
    private List<RsEventDto> rsEventDtos;
}
