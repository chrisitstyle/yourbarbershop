package pl.barbershopproject.barbershop.utils;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of an appointment reservation")
public enum OrderStatus {

    @Schema(description = "New appointment created and scheduled")
    NOWE,

    @Schema(description = "Appointment service completed")
    ZREALIZOWANE,

    @Schema(description = "Appointment cancelled")
    ANULOWANE
}
