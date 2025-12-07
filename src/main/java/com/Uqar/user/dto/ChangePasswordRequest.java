package com.Uqar.user.dto;

import com.Uqar.utils.annotation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Schema
public class ChangePasswordRequest {

    private String currentPassword;
    @ValidPassword
    private String newPassword;
    @ValidPassword
    private String ConfirmPassword;
}
