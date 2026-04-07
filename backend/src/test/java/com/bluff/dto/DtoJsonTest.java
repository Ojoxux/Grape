package com.bluff.dto;

import com.bluff.model.ActionType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void createGameRequest_deserializesAndSerializes() throws Exception {
        String json = "{\"name\":\"alice\",\"cpuCount\":3}";
        CreateGameRequest parsed = mapper.readValue(json, CreateGameRequest.class);
        assertThat(parsed.name()).isEqualTo("alice");
        assertThat(parsed.cpuCount()).isEqualTo(3);
        assertThat(mapper.readTree(mapper.writeValueAsString(parsed))).isEqualTo(mapper.readTree(json));
    }

    @Test
    void createGameResponse_roundTrip() throws Exception {
        CreateGameResponse r = new CreateGameResponse("g-1", "p-1");
        String out = mapper.writeValueAsString(r);
        CreateGameResponse back = mapper.readValue(out, CreateGameResponse.class);
        assertThat(back).isEqualTo(r);
    }

    @Test
    void joinRequestAndResponse_roundTrip() throws Exception {
        JoinRequest jr = mapper.readValue("{\"name\":\"bob\"}", JoinRequest.class);
        assertThat(jr.name()).isEqualTo("bob");
        JoinResponse jresp = new JoinResponse("pid");
        assertThat(mapper.readValue(mapper.writeValueAsString(jresp), JoinResponse.class)).isEqualTo(jresp);
    }

    @Test
    void startRequest_roundTrip() throws Exception {
        StartRequest s = mapper.readValue("{\"playerId\":\"h1\"}", StartRequest.class);
        assertThat(s.playerId()).isEqualTo("h1");
    }

    @Test
    void actionRequest_bidAndChallenge() throws Exception {
        ActionRequest bid =
                mapper.readValue(
                        "{\"type\":\"BID\",\"playerId\":\"p1\",\"quantity\":3,\"face\":5}",
                        ActionRequest.class);
        assertThat(bid.type()).isEqualTo(ActionType.BID);
        assertThat(bid.quantity()).isEqualTo(3);
        assertThat(bid.face()).isEqualTo(5);

        ActionRequest ch =
                mapper.readValue("{\"type\":\"CHALLENGE\",\"playerId\":\"p1\"}", ActionRequest.class);
        assertThat(ch.type()).isEqualTo(ActionType.CHALLENGE);
        assertThat(ch.quantity()).isNull();
        assertThat(ch.face()).isNull();
    }

    @Test
    void gameResponse_playersExposeDiceCountNotDiceField() throws Exception {
        GameResponse gr =
                new GameResponse(
                        "gid",
                        "PLAYING",
                        List.of(new PlayerResponse("p1", "me", false, 5, false)),
                        "p1",
                        new BidResponse(2, 3, "p0"),
                        "host",
                        null,
                        List.of(1, 2, 6),
                        List.of());
        String json = mapper.writeValueAsString(gr);
        JsonNode root = mapper.readTree(json);
        assertThat(root.path("players").path(0).has("diceCount")).isTrue();
        assertThat(root.path("players").path(0).has("dice")).isFalse();
        assertThat(root.path("myDice").isArray()).isTrue();
        GameResponse back = mapper.readValue(json, GameResponse.class);
        assertThat(back.players().getFirst().diceCount()).isEqualTo(5);
    }

    @Test
    void gameSummaryResponse_roundTrip() throws Exception {
        GameSummaryResponse s = new GameSummaryResponse("g", "WAITING", 2);
        assertThat(mapper.readValue(mapper.writeValueAsString(s), GameSummaryResponse.class)).isEqualTo(s);
    }
}
