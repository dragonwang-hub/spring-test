package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RsService {
    final RsEventRepository rsEventRepository;
    final UserRepository userRepository;
    final VoteRepository voteRepository;

    final TradeRepository tradeRepository;

    public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, TradeRepository tradeRepository) {
        this.rsEventRepository = rsEventRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.tradeRepository = tradeRepository;
    }

    public void vote(Vote vote, int rsEventId) {
        Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
        Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
        if (!rsEventDto.isPresent()
                || !userDto.isPresent()
                || vote.getVoteNum() > userDto.get().getVoteNum()) {
            throw new RuntimeException();
        }
        VoteDto voteDto =
                VoteDto.builder()
                        .localDateTime(vote.getTime())
                        .num(vote.getVoteNum())
                        .rsEvent(rsEventDto.get())
                        .user(userDto.get())
                        .build();
        voteRepository.save(voteDto);
        UserDto user = userDto.get();
        user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
        userRepository.save(user);
        RsEventDto rsEvent = rsEventDto.get();
        rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
        rsEventRepository.save(rsEvent);
    }

    public void buy(Trade trade, int rsEventId) {
        Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
        if (!rsEventDto.isPresent()) {
            throw new RuntimeException();
        }
        Optional<TradeDto> rankIsBuyed = Optional.ofNullable(tradeRepository.findByRank(trade.getRank()));

        boolean IsSuccessOfBuyRank = false;
        // 判断是否存在rank已被购买，并判断是否删除替换
        if (!rankIsBuyed.isPresent()) {
            IsSuccessOfBuyRank = true;
        } else {
            // 新购买的记录的Amount 大于 原处于rank记录
            if (trade.getAmount() > rankIsBuyed.get().getAmount()) {
                tradeRepository.delete(rankIsBuyed.get());
                IsSuccessOfBuyRank = true;
            }
        }
        if(!IsSuccessOfBuyRank){
            throw new RuntimeException();
        }

        TradeDto tradeDto = TradeDto.builder()
                .amount(trade.getAmount())
                .rank(trade.getRank())
                .rsEvent(rsEventDto.get())
                .build();
        tradeRepository.save(tradeDto);
        RsEventDto rsEvent = rsEventDto.get();
        rsEvent.setRsRank(tradeDto.getRank());
        rsEvent.setTrade(tradeDto);
        rsEventRepository.save(rsEvent);
    }
}
