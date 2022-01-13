package com.robotikflow.api.server.models.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserCreateRequest
    extends AuthLoginRequest
{
    @NotNull
	@Size(min=3, max=32)
    private String nick;

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
}
