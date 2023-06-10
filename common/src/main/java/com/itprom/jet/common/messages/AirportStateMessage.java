package com.itprom.jet.common.messages;

import com.itprom.jet.common.bean.AirPort;
import com.itprom.jet.common.bean.Source;
import com.itprom.jet.common.bean.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AirportStateMessage extends Message{
    private AirPort airPort;
    protected AirportStateMessage() {
        this.source = Source.AIRPORT;
        this.type = Type.STATE;
    }

    public AirportStateMessage(AirPort airPort) {
        this();
        this.airPort = airPort;
    }
}
