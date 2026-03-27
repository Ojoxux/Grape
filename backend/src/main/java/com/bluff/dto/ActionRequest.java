package com.bluff.dto;

import com.bluff.model.ActionType;

public record ActionRequest(ActionType type, String playerId, Integer quantity, Integer face) {}
