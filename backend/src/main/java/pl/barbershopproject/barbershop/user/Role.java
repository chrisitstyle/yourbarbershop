package pl.barbershopproject.barbershop.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User account roles within the application")
public enum Role {

    @Schema(description = "Standard system user with client privileges")
    USER,

    @Schema(description = "System administrator with full management privileges")
    ADMIN
}

