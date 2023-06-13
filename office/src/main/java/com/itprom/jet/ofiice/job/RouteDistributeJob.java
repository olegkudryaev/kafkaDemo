package com.itprom.jet.ofiice.job;

import com.itprom.jet.common.bean.AirPort;
import com.itprom.jet.common.bean.Board;
import com.itprom.jet.common.bean.Route;
import com.itprom.jet.common.bean.RoutePath;
import com.itprom.jet.common.messages.AirportStateMessage;
import com.itprom.jet.common.messages.OfficeRouteMessage;
import com.itprom.jet.common.processor.MessageConverter;
import com.itprom.jet.ofiice.provider.AirPortsProvider;
import com.itprom.jet.ofiice.provider.BoardsProvider;
import com.itprom.jet.ofiice.service.PathService;
import com.itprom.jet.ofiice.service.WaitingRoutesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RouteDistributeJob {

    private final PathService pathService;
    private final BoardsProvider boardsProvider;
    private final WaitingRoutesService waitingRoutesService;
    private final AirPortsProvider airPortsProvider;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessageConverter messageConverter;

    @Scheduled(initialDelay = 500, fixedDelay = 2500)
    private void distribute(){
        waitingRoutesService.list().stream()
                .filter(Route::notAssigned)
                .forEach(route -> {
                    String startLocation = route.getPath().get(0).getFrom().getName();

                    boardsProvider.getBoards().stream()
                            .filter(board -> startLocation.equals(board.getLocation()) && board.noBusy())
                            .findFirst()
                            .ifPresent(board -> sendBoardToRoute(route, board));

                    if(route.notAssigned()){
                        boardsProvider.getBoards().stream()
                                .filter(Board::noBusy)
                                .findFirst()
                                .ifPresent(board -> {
                                    String currentLocation = board.getLocation();
                                    if(currentLocation.equals(startLocation)){
                                        RoutePath routePath = pathService.makePath(currentLocation, startLocation);
                                        route.getPath().add(0, routePath);
                                    }
                                    sendBoardToRoute(route, board);
                                });
                    }
                });
    }

    private void sendBoardToRoute(Route route, Board board){
        route.setBoardName(board.getName());
        AirPort airPort = airPortsProvider.findAirPortAndRemoveBoard(board.getName());
        board.setLocation(null);
        kafkaTemplate.sendDefault(messageConverter.toJson(new OfficeRouteMessage(route)));
        kafkaTemplate.sendDefault(messageConverter.toJson(new AirportStateMessage(airPort)));
    }

}
