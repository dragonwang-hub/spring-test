package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.exception.Error;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.service.RsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@Validated
public class RsController {
    @Autowired
    RsEventRepository rsEventRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RsService rsService;

    @GetMapping("/rs/list")
    public ResponseEntity<List<RsEvent>> getRsEventListBetween(
            @RequestParam(required = false) Integer start, @RequestParam(required = false) Integer end) {
        List<RsEvent> rsEvents =
                rsEventRepository.findAll().stream()
                        .map(
                                item ->
                                        RsEvent.builder()
                                                .eventName(item.getEventName())
                                                .keyword(item.getKeyword())
                                                .userId(item.getId())
                                                .voteNum(item.getVoteNum())
                                                .build())
                        .collect(Collectors.toList());
        if (start == null || end == null) {
            return ResponseEntity.ok(rsEvents);
        }
        return ResponseEntity.ok(rsEvents.subList(start - 1, end));
    }

    @GetMapping("/rs/{index}")
    public ResponseEntity<RsEvent> getRsEvent(@PathVariable int index) {
        List<RsEvent> rsEvents =
                rsEventRepository.findAll().stream()
                        .map(
                                item ->
                                        RsEvent.builder()
                                                .eventName(item.getEventName())
                                                .keyword(item.getKeyword())
                                                .userId(item.getId())
                                                .voteNum(item.getVoteNum())
                                                .build())
                        .collect(Collectors.toList());
        if (index < 1 || index > rsEvents.size()) {
            throw new RequestNotValidException("invalid index");
        }
        return ResponseEntity.ok(rsEvents.get(index - 1));
    }

    @PostMapping("/rs/event")
    public ResponseEntity addRsEvent(@RequestBody RsEvent rsEvent) {
        Optional<UserDto> userDto = userRepository.findById(rsEvent.getUserId());
        if (!userDto.isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        RsEventDto build =
                RsEventDto.builder()
                        .keyword(rsEvent.getKeyword())
                        .eventName(rsEvent.getEventName())
                        .voteNum(0)
                        .user(userDto.get())
                        .build();
        rsEventRepository.save(build);
        return ResponseEntity.created(null).build();
    }

    @PostMapping("/rs/vote/{id}")
    public ResponseEntity vote(@PathVariable int id, @RequestBody Vote vote) {
        rsService.vote(vote, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rs/buy/{id}")
    public ResponseEntity buy(@PathVariable int id, @RequestBody Trade trade) {
        rsService.buy(trade, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rs/sortedevents")
    public ResponseEntity<List<RsEvent>> getAllRsEventByOrderDesc() {
        List<RsEvent> rsEvents = rsEventRepository.findAll().stream()
                .map(item -> RsEvent.builder()
                        .eventName(item.getEventName())
                        .keyword(item.getKeyword())
                        .userId(item.getId())
                        .voteNum(item.getVoteNum())
                        .rsRank(item.getRsRank())
                        .build())
                .collect(Collectors.toList());

        List<RsEvent> rsEventNoBuyRank = rsEvents.stream().filter(rs -> rs.getRsRank() == 0).collect(Collectors.toList());
        rsEventNoBuyRank.sort(new RsEventComparator());

        List<RsEvent> rsEventBuyRank = rsEvents.stream().filter(rs -> rs.getRsRank() != 0).collect(Collectors.toList());
        Map<Integer, RsEvent> rsEventBuyRankMap = new TreeMap<>();
        rsEventBuyRank.forEach(rs -> {
            rsEventBuyRankMap.put(rs.getRsRank(), rs);
        });
        rsEventBuyRankMap.forEach((k, v) -> {//sortAllTopics.add(k, v));
            k = k - 1;
            if (k >= rsEventNoBuyRank.size()) { // 若某个位置热搜覆盖了最后一位的热搜，其位置会因为覆盖向前移动一位。
                rsEventNoBuyRank.add(rsEventNoBuyRank.size(), v);
            } else {
                rsEventNoBuyRank.add(k, v);
            }
        });
        for (int i = 0; i < rsEventNoBuyRank.size(); i++) {
            rsEventNoBuyRank.get(i).setRsRank(i+1);
        }
        return ResponseEntity.ok(rsEventNoBuyRank);
    }


    @ExceptionHandler({RequestNotValidException.class, RuntimeException.class})
    public ResponseEntity<Error> handleRequestErrorHandler(Exception e) {
        Error error = new Error();
        error.setError(e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
}

// 比较器
class RsEventComparator implements Comparator<RsEvent> {
    @Override
    public int compare(RsEvent rsEvent1, RsEvent rsEvent2) {
        int result = 0;
        // 比较用来排序的两个参数。根据第一个参数小于、等于或大于第二个参数分别返回负整数、零或正整数。
        // 总票数排序,升序
        int voteCountSeq = rsEvent1.getVoteNum() - rsEvent2.getVoteNum();
        if (voteCountSeq != 0) {
            result = (voteCountSeq < 0) ? 1 : -1;
        }
        return result;
    }

}