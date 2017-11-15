package it.bologna.ausl.gipi.security.login.bean;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import it.bologna.ausl.gipi.security.UserTokenState;
import it.bologna.ausl.gipi.rest.commons.message.MessageStatus;
import it.bologna.ausl.gipi.rest.commons.message.WithMessageStatus;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotNull;

/**
 * Created by f.longhitano on 14/07/2017.
 */
public class LoginResponseDTO implements WithMessageStatus{

    @NotNull
    @JsonSerialize(as = MessageStatus.class)
    private LoginStatus messageStatus;

    @JsonSerialize(as = UserDetails.class)
    private UserDetails userDetails;

    private UserTokenState userTokenState;


    @Override
    public LoginStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(LoginStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public UserTokenState getUserTokenState() {
        return userTokenState;
    }

    public void setUserTokenState(UserTokenState userTokenState) {
        this.userTokenState = userTokenState;
    }


}