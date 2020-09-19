package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RsEventRepository rsEventRepository;
    @Autowired
    VoteRepository voteRepository;
    @Autowired
    TradeRepository tradeRepository;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        rsEventRepository.deleteAll();
        userRepository.deleteAll();
        userDto =
                UserDto.builder()
                        .voteNum(10)
                        .phone("188888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("idolice")
                        .build();

    }


    @Test
    public void shouldGetRsEventList() throws Exception {
        UserDto save = userRepository.save(userDto);

        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

        rsEventRepository.save(rsEventDto);

        mockMvc
                .perform(get("/rs/list"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
                .andExpect(jsonPath("$[0].keyword", is("无分类")))
                .andExpect(jsonPath("$[0]", not(hasKey("user"))))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldGetOneEvent() throws Exception {
        UserDto save = userRepository.save(userDto);

        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

        rsEventRepository.save(rsEventDto);
        rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
        rsEventRepository.save(rsEventDto);
        mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.eventName", is("第一条事件")));
        mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.keyword", is("无分类")));
        mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.eventName", is("第二条事件")));
        mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.keyword", is("无分类")));
    }

    @Test
    public void shouldGetErrorWhenIndexInvalid() throws Exception {
        mockMvc
                .perform(get("/rs/4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid index")));
    }

    @Test
    public void shouldGetRsListBetween() throws Exception {
        UserDto save = userRepository.save(userDto);

        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
        rsEventRepository.save(rsEventDto);
        rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
        rsEventRepository.save(rsEventDto);
        rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).build();
        rsEventRepository.save(rsEventDto);
        mockMvc
                .perform(get("/rs/list?start=1&end=2"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
                .andExpect(jsonPath("$[0].keyword", is("无分类")))
                .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
                .andExpect(jsonPath("$[1].keyword", is("无分类")));
        mockMvc
                .perform(get("/rs/list?start=2&end=3"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventName", is("第二条事件")))
                .andExpect(jsonPath("$[0].keyword", is("无分类")))
                .andExpect(jsonPath("$[1].eventName", is("第三条事件")))
                .andExpect(jsonPath("$[1].keyword", is("无分类")));
        mockMvc
                .perform(get("/rs/list?start=1&end=3"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].keyword", is("无分类")))
                .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
                .andExpect(jsonPath("$[1].keyword", is("无分类")))
                .andExpect(jsonPath("$[2].eventName", is("第三条事件")))
                .andExpect(jsonPath("$[2].keyword", is("无分类")));
    }

    @Test
    public void shouldAddRsEventWhenUserExist() throws Exception {

        UserDto save = userRepository.save(userDto);

        String jsonValue =
                "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": " + save.getId() + "}";

        mockMvc
                .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        List<RsEventDto> all = rsEventRepository.findAll();
        assertNotNull(all);
        assertEquals(all.size(), 1);
        assertEquals(all.get(0).getEventName(), "猪肉涨价了");
        assertEquals(all.get(0).getKeyword(), "经济");
        assertEquals(all.get(0).getUser().getUserName(), save.getUserName());
        assertEquals(all.get(0).getUser().getAge(), save.getAge());
    }

    @Test
    public void shouldAddRsEventWhenUserNotExist() throws Exception {
        String jsonValue = "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": 100}";
        mockMvc
                .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldVoteSuccess() throws Exception {
        UserDto save = userRepository.save(userDto);
        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
        rsEventDto = rsEventRepository.save(rsEventDto);

        String jsonValue =
                String.format(
                        "{\"userId\":%d,\"time\":\"%s\",\"voteNum\":1}",
                        save.getId(), LocalDateTime.now().toString());
        mockMvc
                .perform(
                        post("/rs/vote/{id}", rsEventDto.getId())
                                .content(jsonValue)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        UserDto userDto = userRepository.findById(save.getId()).get();
        RsEventDto newRsEvent = rsEventRepository.findById(rsEventDto.getId()).get();
        assertEquals(userDto.getVoteNum(), 9);
        assertEquals(newRsEvent.getVoteNum(), 1);
        List<VoteDto> voteDtos = voteRepository.findAll();
        assertEquals(voteDtos.size(), 1);
        assertEquals(voteDtos.get(0).getNum(), 1);
    }

    @Test
    public void shouldBuyRankSuccessWhenTheRankHaveNotBuy() throws Exception {
        UserDto save = userRepository.save(userDto);
        RsEventDto rsEventDto =
                RsEventDto.builder()
                        .eventName("event name")
                        .keyword("keyword")
                        .user(save)
                        .rsRank(10)
                        .build();
        rsEventDto = rsEventRepository.save(rsEventDto);
        Trade trade = Trade.builder()
                .amount(100)
                .rank(1)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonTrade = objectMapper.writeValueAsString(trade);
        mockMvc.perform(post("/rs/buy/{id}", rsEventDto.getId())
                .content(jsonTrade).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        RsEventDto rsEventBuyRank = tradeRepository.findByRank(1).getRsEvent();
        assertEquals(rsEventDto.getEventName(), rsEventBuyRank.getEventName());
    }

    @Test
    public void shouldBuyRsEventFailWhenAmountLessThanRankIsBuyed() throws Exception {
        UserDto save = userRepository.save(userDto);
        RsEventDto rsEventDto = RsEventDto.builder()
                .eventName("event name")
                .keyword("keyword")
                .user(save)
                .rsRank(10)
                .build();
        rsEventDto = rsEventRepository.save(rsEventDto);
        TradeDto tradeDto = TradeDto.builder()
                .amount(100)
                .rank(10)
                .rsEvent(rsEventDto)
                .build();
        tradeRepository.save(tradeDto);
        RsEventDto rsEventDtoFotTestBuy = RsEventDto.builder()
                .eventName("new event name")
                .keyword("keyword")
                .user(save)
                .rsRank(100)
                .build();
        rsEventDto = rsEventRepository.save(rsEventDtoFotTestBuy);
        Trade trade = Trade.builder()
                .amount(100)
                .rank(10)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonTrade = objectMapper.writeValueAsString(trade);
        mockMvc.perform(post("/rs/buy/{id}", rsEventDtoFotTestBuy.getId())
                .content(jsonTrade).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        RsEventDto rsEventRank10 = tradeRepository.findByRank(10).getRsEvent();
        assertEquals("event name",String.valueOf(rsEventRank10.getEventName()));
    }

    @Test
    public void shouldBuyRsEventFailWhenAmountMoreThanRankIsBuyed() throws Exception {
        UserDto save = userRepository.save(userDto);
        RsEventDto rsEventDto = RsEventDto.builder()
                .eventName("event name")
                .keyword("keyword")
                .user(save)
                .rsRank(10)
                .build();
        rsEventDto = rsEventRepository.save(rsEventDto);
        TradeDto tradeDto = TradeDto.builder()
                .amount(10)
                .rank(10)
                .rsEvent(rsEventDto)
                .build();
        tradeRepository.save(tradeDto);
        RsEventDto rsEventDtoFotTestBuy = RsEventDto.builder()
                .eventName("new event name")
                .keyword("keyword")
                .user(save)
                .rsRank(100)
                .build();
        rsEventRepository.save(rsEventDtoFotTestBuy);
        Trade trade = Trade.builder()
                .amount(100)
                .rank(10)
                .build();
        assertEquals(2,rsEventRepository.count());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonTrade = objectMapper.writeValueAsString(trade);
        mockMvc.perform(post("/rs/buy/{id}", rsEventDtoFotTestBuy.getId())
                .content(jsonTrade).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        RsEventDto rsEventRank10 = tradeRepository.findByRank(10).getRsEvent();
        assertEquals(1,rsEventRepository.count());
        assertEquals("new event name",String.valueOf(rsEventRank10.getEventName()));
    }


    // 数据初始化，为了不影响其他已存在测试，在此测试中，单独调用。
    UserDto userDto_1;
    RsEventDto rsEventDto_1;
    private void setData() {
        userDto_1 = UserDto.builder()
                .userName("hello")
                .age(19)
                .gender("male")
                .email("1@2.3")
                .phone("10123456789")
                .build();
        UserDto userDto_2 = UserDto.builder()
                .userName("kitty")
                .age(19)
                .gender("female")
                .email("1@2.3")
                .phone("10123456789")
                .build();
        List<UserDto> userDtoList = new ArrayList<>();
        userDtoList.add(userDto_1);
        userDtoList.add(userDto_2);
        userRepository.saveAll(userDtoList);

        rsEventDto_1 = RsEventDto.builder()
                .eventName("1.猪肉又涨价了啊！")
                .keyword("经济")
                .voteNum(1)
                .user(userDto_1)
                .build();
        RsEventDto rsEventDto_2 = RsEventDto.builder()
                .eventName("2.2号事件")
                .keyword("经济")
                .user(userDto_1)
                .voteNum(2)
                .build();
        RsEventDto rsEventDto_3 = RsEventDto.builder()
                .eventName("3.3号事件")
                .keyword("forTest")
                .user(userDto_1)
                .voteNum(3)
                .build();
        RsEventDto rsEventDto_4 = RsEventDto.builder()
                .eventName("4.4号事件")
                .keyword("forTest")
                .user(userDto_2)
                .voteNum(4)
                .build();
        RsEventDto rsEventDto_5 = RsEventDto.builder()
                .eventName("5.5号事件")
                .keyword("forTest")
                .user(userDto_2)
                .voteNum(5)
                .build();
        RsEventDto rsEventDto_6 = RsEventDto.builder()
                .eventName("6.6号事件")
                .keyword("forTest")
                .user(userDto_2)
                .voteNum(6)
                .build();
        List<RsEventDto> rsEventDtoList = new ArrayList<>();
        rsEventDtoList.add(rsEventDto_1);
        rsEventDtoList.add(rsEventDto_2);
        rsEventDtoList.add(rsEventDto_3);
        rsEventDtoList.add(rsEventDto_4);
        rsEventDtoList.add(rsEventDto_5);
        rsEventDtoList.add(rsEventDto_6);
        rsEventRepository.saveAll(rsEventDtoList);
    }
    @Test
    public void shouldGetAllRsEventBySortWhenNoHaveBuyyedRsEvent() throws Exception {
        setData();
        mockMvc.perform(get("/rs/sortedevents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(6)))
                .andExpect(jsonPath("$[0].eventName",is("6.6号事件")))
                .andExpect(jsonPath("$[0].voteNum",is(6)))
                .andExpect(jsonPath("$[0].rsRank",is(1)))
                .andExpect(jsonPath("$[5].eventName",is("1.猪肉又涨价了啊！")))
                .andExpect(jsonPath("$[5].voteNum",is(1)))
                .andExpect(jsonPath("$[5].rsRank",is(6)));        ;
    }

    @Test
    public void shouldGetAllRsEventBySortWhenHaveBuyyedRsEvent() throws Exception {
        setData();
        TradeDto tradeDto = TradeDto.builder()
                .amount(10)
                .rank(1)
                .rsEvent(rsEventDto_1)
                .build();
        tradeRepository.save(tradeDto);
        rsEventDto_1.setRsRank(tradeDto.getRank());
        rsEventDto_1.setTrade(tradeDto);
        rsEventRepository.save(rsEventDto_1);
        mockMvc.perform(get("/rs/sortedevents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(6)))
                .andExpect(jsonPath("$[0].eventName",is("1.猪肉又涨价了啊！")))
                .andExpect(jsonPath("$[0].voteNum",is(1)))
                .andExpect(jsonPath("$[0].rsRank",is(1)))
                .andExpect(jsonPath("$[5].eventName",is("2.2号事件")))
                .andExpect(jsonPath("$[5].voteNum",is(2)))
                .andExpect(jsonPath("$[5].rsRank",is(6)));
    }
}
