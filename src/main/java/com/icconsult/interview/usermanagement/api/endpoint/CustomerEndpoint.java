/*
 * CustomerEndpoint.java
 *
 * (c) Copyright iC Consult GmbH, 2021
 * All Rights reserved.
 *
 * iC Consult GmbH
 * 45128 Essen
 * Germany
 *
 */

package com.icconsult.interview.usermanagement.api.endpoint;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import com.icconsult.interview.usermanagement.api.dto.CustomerRequest;
import com.icconsult.interview.usermanagement.api.dto.CustomerResponse;
import com.icconsult.interview.usermanagement.behavior.CustomerManagementService;
import com.icconsult.interview.usermanagement.exception.NotAuthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer/{uuid}")
@Validated
public class CustomerEndpoint {

    Logger logger = LoggerFactory.getLogger(CustomerEndpoint.class);

    private final CustomerManagementService customerService;

    @Autowired
    public CustomerEndpoint(CustomerManagementService customerService) {
        this.customerService = customerService;
    }

    @Operation(
            summary = "Get a customer based on their cross system unique identifier",
            description = "Returns the customer object containing all the customer's data.",
            security = @SecurityRequirement(name = "swagger_oauth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer record found successfully.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponse.class))}),
            @ApiResponse(responseCode = "403", description = "The currently authenticated principal is not permitted to access this customer's data",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content)})
    @GetMapping()
    public CustomerResponse getCustomer(@AuthenticationPrincipal Jwt jwt, @Parameter(description = "Cross system customer id (uuid)") @Valid @Pattern(regexp = "^[a-z0-9-]*$") @PathVariable("uuid") String userId) {
        extractUserId(jwt, false);
        return customerService.getCustomer(userId);
    }

    @Operation(
            summary = "Update an already existing customer",
            description = "Updates an already existing customer by overwriting all values with the values provided in the request. Cannot be used to create new customer accounts. ",
            security = @SecurityRequirement(name = "swagger_oauth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer record updated successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponse.class))}),
            @ApiResponse(responseCode = "403", description = "The currently authenticated principal is not permitted to update this customer's data",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content)})
    @PutMapping()
    @ResponseStatus(HttpStatus.OK)
    public CustomerResponse putCustomer (@AuthenticationPrincipal Jwt jwt, @Parameter(description = "Cross system customer id (uuid)") @Valid @Pattern(regexp = "^[a-z0-9-]*$") @PathVariable("uuid") String userId, @RequestBody final CustomerRequest customerRequestValue) {
        return customerService.updateCustomer(userId, extractUserId(jwt, true), customerRequestValue);
    }

    private String extractUserId(Jwt jwt, boolean isWriteOperation) {
        if (jwt != null && jwt.getSubject() != null) {
            logger.trace("Obtained token: " + jwt.getTokenValue());
            String operationType = isWriteOperation ? "write" : "read";
            logger.info("User attempting " + operationType + " operation: [" + jwt.getSubject() + "].");
            return jwt.getSubject();
        } else {
            throw new NotAuthorizedException("No subject assigned.");
        }
    }
}
