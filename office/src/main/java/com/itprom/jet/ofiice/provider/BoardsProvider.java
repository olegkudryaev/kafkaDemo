package com.itprom.jet.ofiice.provider;

import com.itprom.jet.common.bean.Board;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class BoardsProvider {
    private final List<Board> boards = new ArrayList<>();
    private final Lock lock = new ReentrantLock(true);
    public Optional <Board> getBoards(String boardName){
        return boards.stream().filter(board -> board.equals(boardName))
                .findFirst();
    }

    public void addBoards(Board board){
        try {
            lock.lock();
            Optional<Board> optionalBoard = getBoards(board.getName());
            if (optionalBoard.isPresent()) {
                int ind = boards.indexOf(optionalBoard.get());
                boards.set(ind, board);
            }else {
                boards.add(board);
            }
        } finally {
            lock.unlock();
        }
    }

    public List<Board> getBoards() {
        return boards;
    }
}
