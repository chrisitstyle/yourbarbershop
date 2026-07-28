package pl.barbershopproject.barbershop.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit Logs", description = "endpoints for system activity audit logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/admin/audit-logs")
    @Operation(
            summary = "Get all audit logs (Admin only)",
            description = "Retrieves a chronological list of all system audit events, newest first."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of audit logs",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AuditLogDTO.class))
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing or invalid JWT access token",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Access restricted to users with ADMIN role",
            content = @Content
    )
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }
}