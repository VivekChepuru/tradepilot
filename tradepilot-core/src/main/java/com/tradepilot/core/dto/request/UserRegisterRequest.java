package com.tradepilot.core.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {
    private String username;
    private String phoneNumber;
    private String password;
}
