package pl.barbershopproject.barbershop.offer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.barbershopproject.barbershop.offer.dto.OfferCreationDTO;
import pl.barbershopproject.barbershop.offer.dto.UpdateOfferDTO;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/offers")
@Tag(name = "Offers", description = "Management of service offers available in the barbershop")
class OfferController {

    private final OfferService offerService;

    @Operation(summary = "Add a new offer", description = "Creates a new service offer. Restricted to ADMIN users.")
    @ApiResponse(responseCode = "201", description = "Offer successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid input data provided")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Access restricted to administrators")
    @PostMapping
    public ResponseEntity<Offer> addOffer(@Valid @RequestBody OfferCreationDTO offerCreationDTO) {
        Offer savedOffer = offerService.addOffer(offerCreationDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedOffer.getIdOffer())
                .toUri();

        return ResponseEntity.created(location).body(savedOffer);
    }

    @Operation(summary = "Get all offers", description = "Retrieves a list of all active service offers. Publicly accessible.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of offers")
    @SecurityRequirements()
    @GetMapping
    public List<Offer> getAllOffers() {
        return offerService.getAllOffers();
    }

    @Operation(summary = "Get a single offer by ID", description = "Retrieves details of a specific service offer. Publicly accessible.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved offer details")
    @ApiResponse(responseCode = "404", description = "Offer not found with the specified ID")
    @SecurityRequirements()
    @GetMapping("/{idOffer}")
    public Offer getSingleOffer(@PathVariable Long idOffer) {
        return offerService.getSingleOffer(idOffer);
    }

    @Operation(summary = "Update an offer", description = "Updates an existing service offer. Restricted to ADMIN users.")
    @ApiResponse(responseCode = "200", description = "Offer successfully updated")
    @ApiResponse(responseCode = "400", description = "Invalid input data provided")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Access restricted to administrators")
    @ApiResponse(responseCode = "404", description = "Offer not found with the specified ID")
    @PutMapping("/{idOffer}")
    public Offer updateOffer(
            @Valid @RequestBody UpdateOfferDTO updatedOffer,
            @PathVariable Long idOffer
    ) {
        return offerService.updateOffer(updatedOffer, idOffer);
    }

    @Operation(summary = "Delete an offer", description = "Deletes a service offer from the database. Restricted to ADMIN users.")
    @ApiResponse(responseCode = "204", description = "Offer successfully deleted")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Access restricted to administrators")
    @ApiResponse(responseCode = "404", description = "Offer not found with the specified ID")
    @DeleteMapping("/{idOffer}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOfferById(@PathVariable Long idOffer) {
        offerService.deleteOfferById(idOffer);
    }
}