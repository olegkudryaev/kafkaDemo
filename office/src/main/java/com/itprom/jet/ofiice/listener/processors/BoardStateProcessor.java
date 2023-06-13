package com.itprom.jet.ofiice.listener.processors;

import com.itprom.jet.common.bean.AirPort;
import com.itprom.jet.common.bean.Board;
import com.itprom.jet.common.bean.Route;
import com.itprom.jet.common.messages.AirportStateMessage;
import com.itprom.jet.common.messages.BoardStateMessage;
import com.itprom.jet.common.processor.MessageConverter;
import com.itprom.jet.common.processor.MessageProcessor;
import com.itprom.jet.ofiice.provider.AirPortsProvider;
import com.itprom.jet.ofiice.provider.BoardsProvider;
import com.itprom.jet.ofiice.service.WaitingRoutesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component("BOARD_STATE")
@RequiredArgsConstructor
public class BoardStateProcessor implements MessageProcessor<BoardStateMessage> {

    private final MessageConverter messageConverter;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final WaitingRoutesService waitingRoutesService;
    private final BoardsProvider boardsProvider;
    private final AirPortsProvider airPortsProvider;


    @Override
    public void process(String jsonMessage) {
        BoardStateMessage message = messageConverter.extractMessage(jsonMessage, BoardStateMessage.class);
        Board board = message.getBoard();
        Optional<Board> previousOpt = boardsProvider.getBoards(board.getName());
        AirPort airPort = airPortsProvider.getAirPort(board.getLocation());

        boardsProvider.addBoards(board);
        if (previousOpt.isPresent() && board.hasRoute() && previousOpt.get().hasRoute()){
            Route route = board.getRoute();
            waitingRoutesService.remove(route);
        }

        if(previousOpt.isEmpty() || !board.isBusy() && previousOpt.get().isBusy()){
            airPort.addBoard(board.getName());
            kafkaTemplate.sendDefault(messageConverter.toJson(new AirportStateMessage(airPort)));
        }
    }
}
